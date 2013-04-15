/*
 * "Copyright (c) 2013   Capgemini Technology Services (hereinafter "Capgemini")
 *
 * License/Terms of Use
 * Permission is hereby granted, free of charge and for the term of intellectual
 * property rights on the Software, to any person obtaining a copy of this software
 * and associated documentation files (the "Software"), to use, copy, modify and
 * propagate free of charge, anywhere in the world, all or part of the Software
 * subject to the following mandatory conditions:
 *
 * �   The above copyright notice and this permission notice shall be included in
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


package com.capgemini.web.wuic.configuration.impl;

import com.capgemini.web.wuic.configuration.BadConfigurationException;
import com.capgemini.web.wuic.configuration.Configuration;
import com.capgemini.web.wuic.configuration.DomConfigurationBuilder;
import com.capgemini.web.wuic.configuration.YuiConfiguration;

import net.sf.ehcache.Cache;

import org.apache.commons.lang.math.NumberUtils;
import org.w3c.dom.Node;

/**
 * <p>
 * This builders produces {@link YuiJavascriptConfigurationDomBuilder} instances.
 * </p>
 * 
 * @author Guillaume DROUET
 * @version 1.0
 * @since 0.1.0
 */
public class YuiJavascriptConfigurationDomBuilder implements DomConfigurationBuilder {

    /**
     * {@inheritDoc}
     */
    public Configuration build(final Node nodeConfiguration, final Cache cache) throws BadConfigurationException {
        // Expected elements
        String compress = null;
        String aggregate = null;
        String lineBreakPos = null;
        String charset = null;
        String munge = null;
        String verbose = null;
        String preserveSemiColons = null;
        String disableOptimizations = null;

        // Find in the children of the given node the expected elements
        for (int i = 0; i < nodeConfiguration.getChildNodes().getLength(); i++) {
            final Node child = nodeConfiguration.getChildNodes().item(i);
            
            if ("compress".equals(child.getNodeName())) {
                compress = child.getFirstChild().getTextContent();
            } else if ("aggregate".equals(child.getNodeName())) {
                aggregate = child.getFirstChild().getTextContent();
            } else if ("charset".equals(child.getNodeName())) {
                charset = child.getFirstChild().getTextContent();
            } else if ("yuiLineBreakPos".equals(child.getNodeName())) {
                lineBreakPos = child.getFirstChild().getTextContent();
            } else if ("yuiMunge".equals(child.getNodeName())) {
                munge = child.getFirstChild().getTextContent();
            } else if ("yuiPreserveAllSemiColons".equals(child.getNodeName())) {
                preserveSemiColons = child.getFirstChild().getTextContent();
            } else if ("yuiVerbose".equals(child.getNodeName())) {
                verbose = child.getFirstChild().getTextContent();
            } else if ("yuiDisableOptimizations".equals(child.getNodeName())) {
                disableOptimizations = child.getFirstChild().getTextContent();
            }
        }
        
        // If one element is null, then throw an exception
        if (compress == null || aggregate == null || lineBreakPos == null || charset == null
                || munge == null || preserveSemiColons == null || verbose == null || disableOptimizations == null) {
            final String message = "Elements compress, aggregate, charset, yuiMunge, yuiPreserveAllSemiColons, "
            		+ "yuiDisableOptimizations, yuiVerbose and yuiLineBreakPos must be defined";
            throw new BadConfigurationException(message);
        // Check number consistency
        } else if (!NumberUtils.isNumber(lineBreakPos)) {
            throw new BadConfigurationException("lineBreakPos element must be an integer");
        }
        
        // Check if the ID exists to get it
        final Node idAttribute = nodeConfiguration.getAttributes().getNamedItem("id");
        
        if (idAttribute == null) {
            throw new BadConfigurationException("id has not been found");
        }
        
        final String id = idAttribute.getNodeValue();
        
        // Create and return the configuration
        final YuiConfiguration base = new YuiConfigurationImpl(id,
                Boolean.parseBoolean(compress),
                Boolean.parseBoolean(aggregate),
                Integer.parseInt(lineBreakPos),
                charset,
                cache);

        return new YuiJavascriptConfigurationImpl(base,
                Boolean.parseBoolean(munge),
                Boolean.parseBoolean(verbose),
                Boolean.parseBoolean(preserveSemiColons),
                Boolean.parseBoolean(disableOptimizations));
    }
}