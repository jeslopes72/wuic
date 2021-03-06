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


package com.github.wuic.engine.impl.embedded;

import com.github.wuic.NutType;
import com.github.wuic.engine.Region;
import com.github.wuic.exception.wrapper.StreamException;
import com.github.wuic.nut.core.ByteArrayNut;
import com.github.wuic.nut.Nut;
import com.github.wuic.util.IOUtils;

import java.util.List;

/**
 * <p>
 * This {@link com.github.wuic.engine.SpriteProvider} generates CSS code to represent the sprites.
 * </p>
 *
 * @author Guillaume DROUET
 * @version 1.6
 * @since 0.3.1
 */
public class CGCssSpriteProvider extends CGAbstractSpriteProvider {

    /**
     * {@inheritDoc}
     */
    @Override
    public Nut getSprite(final String url, final String workflowId, final String nutNameSuffix, final List<Nut> originals)
            throws StreamException {
        final ByteArrayNut retval = new ByteArrayNut(nutNameSuffix + "sprites.css", NutType.CSS, originals);
        final StringBuilder cssBuilder = new StringBuilder();

        for (String name : regions.keySet()) {
            final Region reg = regions.get(name);
            final String className = convertAllowedName(workflowId, name);

            // Define region within the image
            cssBuilder.append(".");
            cssBuilder.append(className);
            cssBuilder.append("{display:inline-block;background:url('");
            cssBuilder.append(IOUtils.mergePath("/", url, retval.getVersionNumber().toString(), image));
            cssBuilder.append("') ");
            cssBuilder.append(String.valueOf(reg.getxPosition() * -1));
            cssBuilder.append("px ");
            cssBuilder.append(String.valueOf(reg.getyPosition() * -1));
            cssBuilder.append("px;width:");
            cssBuilder.append(String.valueOf((int) reg.getWidth()));
            cssBuilder.append("px;height:");
            cssBuilder.append(String.valueOf((int) reg.getHeight()));
            cssBuilder.append("px;}");
        }

        // Make a byte array and return wrapper nut
        retval.setBytes(cssBuilder.toString().getBytes());
        return retval;
    }
}
