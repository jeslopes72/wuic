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

import com.github.wuic.Context;
import com.github.wuic.ContextBuilder;
import com.github.wuic.NutType;
import com.github.wuic.engine.Engine;
import com.github.wuic.engine.EngineBuilderFactory;
import com.github.wuic.engine.EngineRequest;
import com.github.wuic.engine.NodeEngine;
import com.github.wuic.engine.impl.embedded.CGCssInspectorEngine;
import com.github.wuic.nut.Nut;
import com.github.wuic.nut.NutDao;
import com.github.wuic.nut.NutsHeap;
import com.github.wuic.xml.FileXmlContextBuilderConfigurator;
import junit.framework.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.io.ByteArrayInputStream;
import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * <p>
 * {@link com.github.wuic.engine.impl.embedded.CGCssInspectorEngine} tests.
 * </p>
 *
 * @author Guillaume DROUET
 * @version 1.3
 * @since 0.4.1
 */
@RunWith(JUnit4.class)
public class CssInspectorTest {

    /**
     * <p>
     * Test when multiple @import/background are declared on the same line.
     * </p>
     *
     * @throws Exception if test fails
     */
    @Test
    public void multipleImportPerLineTest() throws Exception {
        final AtomicInteger createCount = new AtomicInteger(0);
        final NutDao dao = Mockito.mock(NutDao.class);
        Mockito.when(dao.withRootPath(Mockito.anyString())).thenReturn(dao);
        final Engine engine = new CGCssInspectorEngine(true, "UTF-8");
        final NutsHeap heap = Mockito.mock(NutsHeap.class);
        Mockito.when(dao.create(Mockito.anyString(), Mockito.any(NutDao.PathFormat.class))).thenAnswer(new Answer<Object>() {

            /**
             * {@inheritDoc}
             */
            @Override
            public Object answer(final InvocationOnMock invocationOnMock) throws Throwable {
                final List<Nut> retval = new ArrayList<Nut>();
                final Nut nut = Mockito.mock(Nut.class);
                Mockito.when(nut.getName()).thenReturn(String.valueOf(createCount.incrementAndGet()));
                Mockito.when(nut.getNutType()).thenReturn(NutType.CSS);
                Mockito.when(nut.openStream()).thenReturn(new ByteArrayInputStream("".getBytes()));
                Mockito.when(nut.getVersionNumber()).thenReturn(new BigInteger("1"));

                retval.add(nut);
                return retval;
            }
        });
        Mockito.when(heap.getId()).thenReturn("heap");
        Mockito.when(heap.hasCreated(Mockito.any(Nut.class))).thenReturn(true);
        Mockito.when(heap.findDaoFor(Mockito.any(Nut.class))).thenReturn(dao);
        final NutsHeap h = new NutsHeap(null, dao, "heap", heap);
        final List<Nut> nuts = new ArrayList<Nut>();
        final Nut nut = Mockito.mock(Nut.class);
        Mockito.when(nut.getVersionNumber()).thenReturn(new BigInteger("1"));
        Mockito.when(nut.getNutType()).thenReturn(NutType.CSS);
        Mockito.when(nut.getName()).thenAnswer(new Answer<Object>() {

            /**
             * {@inheritDoc}
             */
            @Override
            public Object answer(final InvocationOnMock invocationOnMock) throws Throwable {
                final String retval = createCount.get() + ".css";
                h.getCreated().add(retval);
                return retval;

            }
        });
        nuts.add(nut);

        String[][] collection = new String[][] {
            new String[] {"@import url(\"%s\")", "jquery.ui.core.css"},
                new String[] {"@import \"%s\";", "jquery.ui.accordion.css"},
                new String[] {"@import '%s';", "jquery.ui.autocomplete.css"},
                new String[] {"@import url('%s');", "jquery.ui.button.css"},
                new String[] {"@import \"%s\";", "jquery.ui.datepicker.css"},
                new String[] {"@import '%s';", "jquery.ui.dialog.css"},
                new String[] {"@import url(  \"%s\");", "jquery.ui.menu.css"},
                new String[] {"background: url(\"%s\");", "sprite.png"},
                new String[] {"background: url(%s);", "sprite2.png"},
                new String[] {"background: url('%s');", "sprite3.png"},
                new String[] {"background: #FFF url('%s');", "sprite4.png"},
                new String[] {"@import /* some comments */ url(\"%s\");", "jquery.ui.spinner.css"},
        };

        final StringBuilder builder = new StringBuilder();
        int create = 0;

        for (final String[] c : collection) {
            final String rule = c[0];
            final String path = c[1];
            builder.append(String.format(rule, path));
            create++;
        }

        // ignore comments
        builder.append("/*background: url('sprite5.png');*/");
        builder.append("/*background:\n url('sprite6.png');*/");

        Mockito.when(nut.openStream()).thenReturn(new ByteArrayInputStream(builder.toString().getBytes()));
        Mockito.when(heap.getNuts()).thenReturn(nuts);
        Mockito.when(heap.getNutDao()).thenReturn(dao);
        Mockito.when(heap.findDaoFor(Mockito.mock(Nut.class))).thenReturn(dao);
        Mockito.when(heap.getCreated()).thenReturn(new HashSet<String>());

        final EngineRequest request = new EngineRequest("wid", "cp", h, new HashMap<NutType, NodeEngine>());
        engine.parse(request);
        Assert.assertEquals(createCount.get(), create);
    }
    
    /**
     * <p>
     * Test when @import with "data:" url.
     * </p>
     *
     * @throws Exception if test fails
     */
    @Test
    public void dataUrlTest() throws Exception {
        final AtomicInteger createCount = new AtomicInteger(0);
        final NutDao dao = Mockito.mock(NutDao.class);
        Mockito.when(dao.withRootPath(Mockito.anyString())).thenReturn(dao);
        final Engine engine = new CGCssInspectorEngine(true, "UTF-8");
        final NutsHeap heap = Mockito.mock(NutsHeap.class);
        Mockito.when(dao.create(Mockito.anyString(), Mockito.any(NutDao.PathFormat.class))).thenAnswer(new Answer<Object>() {

            /**
             * {@inheritDoc}
             */
            @Override
            public Object answer(final InvocationOnMock invocationOnMock) throws Throwable {
                final List<Nut> retval = new ArrayList<Nut>();
                final Nut nut = Mockito.mock(Nut.class);
                Mockito.when(nut.getName()).thenReturn(String.valueOf(createCount.incrementAndGet()));
                Mockito.when(nut.getNutType()).thenReturn(NutType.CSS);
                Mockito.when(nut.openStream()).thenReturn(new ByteArrayInputStream("".getBytes()));
                Mockito.when(nut.getVersionNumber()).thenReturn(new BigInteger("1"));

                retval.add(nut);
                return retval;
            }
        });
        Mockito.when(heap.getId()).thenReturn("heap");
        Mockito.when(heap.hasCreated(Mockito.any(Nut.class))).thenReturn(true);
        Mockito.when(heap.findDaoFor(Mockito.any(Nut.class))).thenReturn(dao);
        final NutsHeap h = new NutsHeap(null, dao, "heap", heap);
        final List<Nut> nuts = new ArrayList<Nut>();
        final Nut nut = Mockito.mock(Nut.class);
        Mockito.when(nut.getVersionNumber()).thenReturn(new BigInteger("1"));
        Mockito.when(nut.getNutType()).thenReturn(NutType.CSS);
        Mockito.when(nut.getName()).thenAnswer(new Answer<Object>() {

            /**
             * {@inheritDoc}
             */
            @Override
            public Object answer(final InvocationOnMock invocationOnMock) throws Throwable {
                final String retval = createCount.get() + ".css";
                h.getCreated().add(retval);
                return retval;

            }
        });
        nuts.add(nut);

        String[][] collection = new String[][] {
            new String[] {"@import url(\"%s\");", "data:image/gif;base64,R0lGODlhCwAHAIAAACgoKP///yH5BAEAAAEALAAAAAALAAcAAAIORI4JlrqN1oMSnmmZDQUAOw=="},
            new String[] {"@import url(\"%s\");", "data:image/svg+xml;utf8,<svg xmlns=\'http://www.w3.org/2000/svg\'><filter id=\'jstree-grayscale\'><feColorMatrix type=\'matrix\' values=\'0.3333 0.3333 0.3333 0 0 0.3333 0.3333 0.3333 0 0 0.3333 0.3333 0.3333 0 0 0 0 0 1 0\'/></filter></svg>#jstree-grayscale"},
        };

        final StringBuilder builder = new StringBuilder();

        for (final String[] c : collection) {
            final String rule = c[0];
            final String path = c[1];
            builder.append(String.format(rule, path));
        }

        Mockito.when(nut.openStream()).thenReturn(new ByteArrayInputStream(builder.toString().getBytes()));
        Mockito.when(heap.getNuts()).thenReturn(nuts);
        Mockito.when(heap.getNutDao()).thenReturn(dao);
        Mockito.when(heap.findDaoFor(Mockito.mock(Nut.class))).thenReturn(dao);
        Mockito.when(heap.getCreated()).thenReturn(new HashSet<String>());

        final EngineRequest request = new EngineRequest("wid", "cp", h, new HashMap<NutType, NodeEngine>());
        engine.parse(request);
        Assert.assertEquals("Shouldn't create nuts for 'data:' urls.", createCount.get(), 0);
    }

    /**
     * Test when file is referenced with '../'.
     *
     * @throws Exception if test fails
     */
    @Test
    public void parentRefTest() throws Exception {
        final ContextBuilder builder = new ContextBuilder();
        EngineBuilderFactory.getInstance().newContextBuilderConfigurator().configure(builder);
        new FileXmlContextBuilderConfigurator(getClass().getResource("/wuic-deep.xml")).configure(builder);
        final Context ctx = builder.build();

        // ../ refers a file inside base directory hierarchy
        List<Nut> group = ctx.process("", "css-inner");
        Assert.assertEquals(1, group.size());
        Assert.assertEquals(3, group.get(0).getReferencedNuts().size());

        // ../ refers a file outside base directory hierarchy
        group = ctx.process("", "css-outer");
        Assert.assertEquals(1, group.size());
        Assert.assertEquals(2, group.get(0).getReferencedNuts().size());
    }

    /**
     * <p>
     * Tests a workflow built on top of a composition.
     * </p>
     *
     * @throws Exception if test fails
     */
    @Test
    public void compositionByWorkflowTest() throws Exception {
        final ContextBuilder builder = new ContextBuilder();
        EngineBuilderFactory.getInstance().newContextBuilderConfigurator().configure(builder);
        new FileXmlContextBuilderConfigurator(getClass().getResource("/wuic-deep.xml")).configure(builder);
        final Context ctx = builder.build();
        ctx.process("", "composite");
    }
    
    
}
