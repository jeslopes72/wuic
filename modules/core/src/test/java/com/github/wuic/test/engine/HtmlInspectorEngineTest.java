/*
 * "Copyright (c) 2014   Capgemini Technology Services (hereinafter "Capgemini")
 *
 * License/Terms of Use
 * Permission is hereby granted, free of charge and for the term of intellectual
 * property rights on the Software, to any person obtaining a copy of this software
 * and associated documentation files (the "Software"), to use, copy, modify and
 * propagate free of charge, anywhere in the world, all or part of the Software
 * subject to the following mandatory conditions:
 *
 * -   The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * Any failure to comply with the above shall automatically terminate the license
 * and be construed as a breach of these Terms of Use causing significant harm to
 * Capgemini.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, PEACEFUL ENJOYMENT,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS
 * OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 * Except as contained in this notice, the name of Capgemini shall not be used in
 * advertising or otherwise to promote the use or other dealings in this Software
 * without prior written authorization from Capgemini.
 *
 * These Terms of Use are subject to French law.
 *
 * IMPORTANT NOTICE: The WUIC software implements software components governed by
 * open source software licenses (BSD and Apache) of which CAPGEMINI is not the
 * author or the editor. The rights granted on the said software components are
 * governed by the specific terms and conditions specified by Apache 2.0 and BSD
 * licenses."
 */


package com.github.wuic.test.engine;

import com.github.wuic.ApplicationConfig;
import com.github.wuic.NutType;
import com.github.wuic.engine.Engine;
import com.github.wuic.engine.EngineRequest;
import com.github.wuic.engine.NodeEngine;
import com.github.wuic.engine.core.EhCacheEngineBuilder;
import com.github.wuic.engine.core.MemoryMapCacheEngineBuilder;
import com.github.wuic.engine.impl.ehcache.EhCacheEngine;
import com.github.wuic.engine.impl.embedded.AbstractCacheEngine;
import com.github.wuic.engine.impl.embedded.CGTextAggregatorEngine;
import com.github.wuic.engine.impl.embedded.HtmlInspectorEngine;
import com.github.wuic.engine.impl.embedded.MemoryMapCacheEngine;
import com.github.wuic.nut.Nut;
import com.github.wuic.nut.NutDao;
import com.github.wuic.nut.NutsHeap;
import com.github.wuic.nut.core.ByteArrayNut;
import com.github.wuic.nut.core.DiskNutDao;
import com.github.wuic.test.ehcache.EhCacheEngineTest;
import com.github.wuic.util.IOUtils;
import junit.framework.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mockito;

import java.io.InputStreamReader;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

/**
 * <p>
 * Tests the {@link HtmlInspectorEngine} class.
 * </p>
 *
 * @author Guillaume DROUET
 * @version 1.0
 * @since 0.4.4
 */
@RunWith(JUnit4.class)
public class HtmlInspectorEngineTest {

    /**
     * The regex that should match HTML when transformed during tests.
     */
    private static final String REGEX = ".*?<link rel=\"stylesheet\" type=\"text/css\" href=\"/.*?aggregate.css\" />.*?" +
            "<script type=\"text/javascript\" src=\"/.*?aggregate.js\"></script>.*?" +
            "<link rel=\"stylesheet\" type=\"text/css\" href=\"/.*?aggregate.css\" />.*?" +
            "<link rel=\"stylesheet\" type=\"text/css\" href=\"/.*?aggregate.css\" />.*?" +
            "<script type=\"text/javascript\" src=\"/.*?aggregate.js\"></script>.*?" +
            "<link rel=\"stylesheet\" type=\"text/css\" href=\"/.*?aggregate.css\" />.*?" +
            "<script type=\"text/javascript\" src=\"/.*?aggregate.js\"></script>.*?";

    /**
     * <p>
     * Complete parse test.
     * </p>
     *
     * @throws Exception if test fails
     */
    @Test
    public void parseTest() throws Exception {
        final NutDao dao = new DiskNutDao(getClass().getResource("/html").getFile(), false, null, -1, false, false);
        final NutsHeap heap = new NutsHeap(Arrays.asList("index.html"), dao, "heap");
        final Map<NutType, NodeEngine> chains = new HashMap<NutType, NodeEngine>();
        chains.put(NutType.CSS, new CGTextAggregatorEngine(true));
        chains.put(NutType.JAVASCRIPT, new CGTextAggregatorEngine(true));
        final EngineRequest request = new EngineRequest("workflow", "", heap, chains);
        final List<Nut> nuts = new HtmlInspectorEngine(true, "UTF-8").parse(request);

        Assert.assertEquals(1, nuts.size());

        final String content = IOUtils.readString(new InputStreamReader(nuts.get(0).openStream()));
        System.out.println(content);
        Assert.assertTrue(Pattern.compile(REGEX, Pattern.DOTALL).matcher(content).matches());
    }

    @Test
    public void bestEffortTest() throws Exception {

        final String content = IOUtils.readString(new InputStreamReader(getClass().getResourceAsStream("/html/index.html")));
        final NutDao dao = new DiskNutDao(getClass().getResource("/html").getFile(), false, null, -1, false, false);
        final CountDownLatch countDownLatch = new CountDownLatch(1);

        final Engine engine = new AbstractCacheEngine(true, true) {

            private CacheResult cache;

            /**
             * {@inheritDoc}
             */
            @Override
            public void putToCache(final EngineRequest.Key request, final CacheResult nuts) {
                cache = nuts;

                if (nuts.getDefaultResult() != null) {
                    countDownLatch.countDown();
                }
            }

            /**
             * {@inheritDoc}
             */
            @Override
            public void removeFromCache(final EngineRequest.Key request) {
            }

            /**
             * {@inheritDoc}
             */
            @Override
            public CacheResult getFromCache(final EngineRequest.Key request) {
                return cache;
            }
        };

        final NutsHeap heap = Mockito.mock(NutsHeap.class);
        final Nut nut = new ByteArrayNut(content.getBytes(), "index.html", NutType.HTML, BigInteger.ONE);
        Mockito.when(heap.getNuts()).thenReturn(Arrays.asList(nut));
        Mockito.when(heap.getNutDao()).thenReturn(dao);
        Mockito.when(heap.findDaoFor(Mockito.any(Nut.class))).thenReturn(dao);
        final Map<NutType, NodeEngine> chains = new HashMap<NutType, NodeEngine>();
        chains.put(NutType.HTML, new HtmlInspectorEngine(true, "UTF-8"));
        chains.put(NutType.JAVASCRIPT, new CGTextAggregatorEngine(true));
        chains.put(NutType.CSS, new CGTextAggregatorEngine(true));

        List<Nut> nuts = engine.parse(new EngineRequest("", "", heap, chains));
        String res = IOUtils.readString(new InputStreamReader(nuts.get(0).openStream()));
        Assert.assertEquals(content, res);

        countDownLatch.await(5, TimeUnit.SECONDS);

        nuts = engine.parse(new EngineRequest("", "", heap, chains));
        res = IOUtils.readString(new InputStreamReader(nuts.get(0).openStream()));
        System.out.print(res);
        Assert.assertTrue(Pattern.compile(REGEX, Pattern.DOTALL).matcher(res).matches());
    }

    /**
     * <p>
     * EhCache support test for head engine.
     * </p>
     *
     * @throws Exception if test fails
     */
    @Test
    public void ehCacheSupportTest() throws Exception {
        final EhCacheEngineBuilder builder = new EhCacheEngineBuilder();
        builder.property(ApplicationConfig.CACHE_PROVIDER_CLASS, EhCacheEngineTest.CacheFactory.class.getName())
                .property(ApplicationConfig.BEST_EFFORT, true);
        final EhCacheEngine cache = (EhCacheEngine) builder.build();
        concurrencyTest(cache);
    }


    /**
     * <p>
     * Memory map support test for head engine.
     * </p>
     *
     * @throws Exception if test fails
     */
    @Test
    public void memoryMapSupportTest() throws Exception {
        final MemoryMapCacheEngineBuilder builder = new MemoryMapCacheEngineBuilder();
        builder.property(ApplicationConfig.BEST_EFFORT, true);
        final MemoryMapCacheEngine cache = (MemoryMapCacheEngine) builder.build();
        concurrencyTest(cache);
    }

    /**
     * <p>
     * Runs a concurrency test with given engine.
     * </p>
     *
     * @param cache the head engine
     */
    public void concurrencyTest(final Engine cache) throws Exception {
        final String content = IOUtils.readString(new InputStreamReader(getClass().getResourceAsStream("/html/index.html")));
        final NutDao dao = new DiskNutDao(getClass().getResource("/html").getFile(), false, null, -1, false, false);
        final CountDownLatch countDownLatch = new CountDownLatch(400);

        final NutsHeap heap = Mockito.mock(NutsHeap.class);
        final Nut nut = new ByteArrayNut(content.getBytes(), "index.html", NutType.HTML, BigInteger.ONE);
        Mockito.when(heap.getNuts()).thenReturn(Arrays.asList(nut));
        Mockito.when(heap.getNutDao()).thenReturn(dao);
        Mockito.when(heap.findDaoFor(Mockito.any(Nut.class))).thenReturn(dao);
        final Map<NutType, NodeEngine> chains = new HashMap<NutType, NodeEngine>();
        chains.put(NutType.HTML, new HtmlInspectorEngine(true, "UTF-8"));

        for (long i = countDownLatch.getCount(); i > 0; i--) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        cache.parse(new EngineRequest("", "", heap, chains));
                        countDownLatch.countDown();
                    } catch (Exception e) {
                        e.printStackTrace();
                        Assert.fail(e.getMessage());
                    }
                }
            }).run();
        }

        Assert.assertTrue(countDownLatch.await(5, TimeUnit.SECONDS));
    }
}
