/*
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2001 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *  notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *  notice, this list of conditions and the following disclaimer in
 *  the documentation and/or other materials provided with the
 *  distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *  if any, must include the following acknowledgment:
 *     "This product includes software developed by the
 *    Apache Software Foundation (http://www.apache.org/)."
 *  Alternately, this acknowledgment may appear in the software itself,
 *  if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Axis" and "Apache Software Foundation" must
 *  not be used to endorse or promote products derived from this
 *  software without prior written permission. For written
 *  permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *  nor may "Apache" appear in their name, without prior written
 *  permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */

package org.apache.axis.handlers;

import org.apache.axis.AxisFault;
import org.apache.axis.Constants;
import org.apache.axis.Handler;
import org.apache.axis.MessageContext;
import org.apache.axis.providers.java.RPCProvider;
import org.apache.axis.transport.http.HTTPConstants;
import org.apache.axis.utils.AxisClassLoader;
import org.apache.axis.utils.JavaUtils;
import org.apache.axis.utils.XMLUtils;
import org.apache.axis.utils.compiler.Compiler;
import org.apache.axis.utils.compiler.CompilerError;
import org.apache.axis.utils.compiler.CompilerFactory;
import org.apache.log4j.Category;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;
import java.util.StringTokenizer;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.JarInputStream;
import java.util.jar.Manifest;

/**
 * This handler will use the MC_REALPATH property of the MsgContext to
 * locate a *.jws (JavaWebService) file.  If found it will copy it to a
 * *.java file, compile it and then run it using the RPCDispatchHandler.
 *
 * Todo:
 *   support msg instead of just rpc
 *   allow configurable handler (not just RPCDispatchHandler)
 *
 * @author Doug Davis (dug@us.ibm.com)
 */
public class JWSProcessor extends BasicHandler
{
    static Category category =
            Category.getInstance(JWSProcessor.class.getName());


    public void invoke(MessageContext msgContext) throws AxisFault
    {
        invokeImpl(msgContext, false);
    }

    public void invokeImpl(MessageContext msgContext, boolean doWsdl)
            throws AxisFault
    {
        if (category.isDebugEnabled())
            category.debug(JavaUtils.getMessage("enter00", "JWSProcessor::invoke"));
        try {
            /* Grab the *.jws filename from the context - should have been */
            /* placed there by another handler (ie. HTTPActionHandler)     */
            /***************************************************************/
            Runtime  rt      = Runtime.getRuntime();
            String   jwsFile = msgContext.getStrProp(Constants.MC_REALPATH);
            if (category.isInfoEnabled())
                category.info("jwsFile: " + jwsFile );
            String   jFile   = jwsFile.substring(0, jwsFile.length()-3) +
                    "java" ;
            String   cFile   = jwsFile.substring(0, jwsFile.length()-3) +
                    "class" ;

            if (category.isInfoEnabled()) {
                category.info("jFile: " + jFile );
                category.info("cFile: " + cFile );
            }

            File  f1 = new File( cFile );
            File  f2 = new File( jwsFile );

            /* Get the class */
            /*****************/
            String clsName = null ;
            clsName = msgContext.getStrProp(Constants.MC_RELATIVE_PATH);
            if ( clsName == null ) clsName = f2.getName();
            if ( clsName != null && clsName.charAt(0) == '/' )
                clsName = clsName.substring(1);

            clsName = clsName.substring( 0, clsName.length()-4 );
            clsName = clsName.replace('/', '.');
            
            if (category.isInfoEnabled())
                category.info("ClsName: " + clsName );

            /* Check to see if we need to recompile */
            /****************************************/
            if ( !f1.exists() || f2.lastModified() > f1.lastModified() ) {
                /* If the class file doesn't exist, or it's older than the */
                /* java file then recompile the java file.                 */
                /* Start by copying the *.jws file to *.java               */
                /***********************************************************/
                category.info(JavaUtils.getMessage("compiling00", jwsFile) );
                category.debug(JavaUtils.getMessage("copy00", jwsFile, jFile) );
                FileReader fr = new FileReader( jwsFile );
                FileWriter fw = new FileWriter( jFile );
                char[] buf = new char[4096];
                int    rc ;
                while ( (rc = fr.read( buf, 0, 4095)) >= 0 )
                    fw.write( buf, 0, rc );
                fw.close();
                fr.close();

                /* Now run javac on the *.java file */
                /************************************/
                category.debug("javac " + jFile );
                // Process proc = rt.exec( "javac " + jFile );
                // proc.waitFor();
                Compiler          compiler = CompilerFactory.getCompiler();
                String            outdir   = null ;
                String[]          args     = null ;

                outdir = msgContext.getStrProp( Constants.MC_HOME_DIR );
                if ( outdir == null ) outdir = f1.getParent();
                if ( outdir == null ) outdir = "." ;

                compiler.setClasspath(getDefaultClasspath(msgContext));
                compiler.setDestination(outdir);
                compiler.setFile(jFile);

                boolean result   = compiler.compile();
                
                /* Delete the temporary *.java file and check return code */
                /**********************************************************/
                (new File(jFile)).delete();

                if ( !result ) {
                    /* Delete the *class file - sometimes it gets created even */
                    /* when there are errors - so erase it so it doesn't       */
                    /* confuse us.                                             */
                    /***********************************************************/
                    (new File(cFile)).delete();

                    Document doc = XMLUtils.newDocument();

                    Element         root = doc.createElementNS("", "Errors");
                    StringBuffer message = new StringBuffer("Error compiling ");
                    message.append(jFile);
                    message.append(":\n");

                    List errors = compiler.getErrors();
                    int count = errors.size();
                    for (int i = 0; i < count; i++) {
                      CompilerError error = (CompilerError) errors.get(i);
                      if (i > 0) message.append("\n");
                      message.append("Line ");
                      message.append(error.getStartLine());
                      message.append(", column ");
                      message.append(error.getStartColumn());
                      message.append(": ");
                      message.append(error.getMessage());
                    }
                    root.appendChild( doc.createTextNode( message.toString() ) );
                    throw new AxisFault( "Server.compileError",
                         JavaUtils.getMessage("badCompile00", jFile),
                        null, new Element[] { root } );
                }
                AxisClassLoader.removeClassLoader( clsName );
            }
            AxisClassLoader cl = msgContext.getClassLoader();
            if ( !cl.isClassRegistered(clsName) )
                cl.registerClass( clsName, cFile );
            msgContext.setClassLoader( cl );

            /* Create a new RPCProvider - this will be the "service"   */
            /* that we invoke.                                                */
            /******************************************************************/
            Handler rpc = new RPCProvider();
            msgContext.setServiceHandler( rpc );

            rpc.setOption( "className", clsName );

            /** For now, allow all methods - we probably want to have a way to
            * configure this in the future.
            */
            rpc.setOption( "allowedMethods", "*");

            rpc.init();   // ??
            if (doWsdl)
                rpc.generateWSDL(msgContext);
            else
                rpc.invoke( msgContext );
            rpc.cleanup();  // ??
        }
        catch( Exception e ) {
            category.error( "JWSProcessor Exception", e );
            throw AxisFault.makeFault(e);
        }

        if (category.isDebugEnabled())
            category.debug(JavaUtils.getMessage("exit00", "JWSProcessor::invoke") );
    }

    public void generateWSDL(MessageContext msgContext) throws AxisFault {
        invokeImpl(msgContext, true);
    }

    private String getDefaultClasspath(MessageContext msgContext)
    {
        StringBuffer classpath = new StringBuffer();
        ClassLoader cl = Thread.currentThread().getContextClassLoader();

        while(cl != null)
        {
            if(cl instanceof URLClassLoader)
            {
                URL[] urls = ((URLClassLoader) cl).getURLs();

                for(int i=0; (urls != null) && i < urls.length; i++)
                {
                    String path = urls[i].getPath();
                    //If it is a drive letter, adjust accordingly.
                    if(path.charAt(0)=='/'&&path.charAt(2)==':')
                        path = path.substring(1);
                    classpath.append(path);
                    classpath.append(File.pathSeparatorChar);

                    // if its a jar extract Class-Path entries from manifest
                    File file = new File(urls[i].getFile());
                    if(file.isFile())
                    {
                        FileInputStream fis = null;

                        try
                        {
                            fis = new FileInputStream(file);

                            if(isJar(fis))
                            {
                                JarFile jar = new JarFile(file);
                                Manifest manifest = jar.getManifest();
                                if (manifest != null)
                                {
                                    Attributes attributes = manifest.
                                            getMainAttributes();
                                    if (attributes != null)
                                    {
                                        String s = attributes.
                           getValue(java.util.jar.Attributes.Name.CLASS_PATH);
                                        String base = file.getParent();

                                        if (s != null)
                                        {
                                            StringTokenizer st =
                                                  new StringTokenizer(s, " ");
                                            while(st.hasMoreTokens())
                                            {
                                                String t = st.nextToken();
                                                classpath.append(base +
                                                      File.separatorChar + t);
                                                classpath.append(
                                                      File.pathSeparatorChar);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        catch(IOException ioe)
                        {
                            if(fis != null)
                                try {
                                    fis.close();
                                } catch (IOException ioe2) {}
                        }
                    }
                }
            }

            cl = cl.getParent();
        }

        // Just to be safe (the above doesn't seem to return the webapp
        // classpath in all cases), manually do this:

        String webBase = (String)msgContext.getProperty(
                                         HTTPConstants.MC_HTTP_SERVLETLOCATION);
        if (webBase != null) {
            classpath.append(webBase + File.separatorChar + "classes" +
                             File.pathSeparatorChar);
            try {
                String libBase = webBase + File.separatorChar + "lib";
                File libDir = new File(libBase);
                String [] jarFiles = libDir.list();
                for (int i = 0; i < jarFiles.length; i++) {
                    String jarFile = jarFiles[i];
                    if (jarFile.endsWith(".jar")) {
                        classpath.append(libBase +
                                         File.separatorChar +
                                         jarFile +
                                         File.pathSeparatorChar);
                    }
                }
            } catch (Exception e) {
                // Oh well.  No big deal.
            }
        }

        // boot classpath isn't found in above search
        if(System.getProperty("sun.boot.class.path") != null) {
            classpath.append(System.getProperty("sun.boot.class.path"));
        }

        return classpath.toString();
    }

    // an exception or emptiness signifies not a jar
    public static boolean isJar(InputStream is)
    {
        try
        {
            JarInputStream jis = new JarInputStream(is);
            if(jis.getNextEntry() != null)
            {
                return true;
            }
        }
        catch(IOException ioe)
        {
        }

        return false;
    }


}