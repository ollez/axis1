/* -*- C++ -*- */

/*
 * The Apache Software License, Version 1.1
 *
 *
 * Copyright (c) 2002 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "SOAP" and "Apache Software Foundation" must
 *    not be used to endorse or promote products derived from this
 *    software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    nor may "Apache" appear in their name, without prior written
 *    permission of the Apache Software Foundation.
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
 *
 *
 *
 *
 * @author Roshan Weerasuriya (roshan@jkcs.slt.lk, roshan@opensource.lk)
 *
 */

#if !defined(AFX_SERVERHELPER_H__INCLUDED_)
#define AFX_SERVERHELPER_H__INCLUDED_

#include <string>
#include <map>

#include "HTTP_KeyWords.h"

using namespace std;

enum CONTENT_TYPE {
	HTTP_KEYWORD_TYPE,
	STRING_TYPE
};

union uHttpMapContent /*this is used to store both the HTTP_KEYWORDS enumerated contents and strings*/
{
	HTTP_KEYWORDS eHTTP_KEYWORD; //for HTTP_KEYWORDS enumerated contents
	const char* msValue; //for string content
};	

struct HTTP_MAP_TYPE {
	CONTENT_TYPE eCONTENT_TYPE;
	uHttpMapContent* objuHttpMapContent;
};

int getSeperatedHTTPParts(string sClientReqStream, string& sHTTPHeaders, string& sHTTPBody, map<HTTP_MAP_KEYWORDS, HTTP_MAP_TYPE*> *map_HTTP_Headers);
int	initializeHeaderMap(const string &HeaderLine, map<HTTP_MAP_KEYWORDS, HTTP_MAP_TYPE*> *map_HTTP_Headers);
int getHttpHeader(HTTP_MAP_KEYWORDS eKeyWord, map<HTTP_MAP_KEYWORDS, HTTP_MAP_TYPE*> *map_HTTP_Headers, HTTP_MAP_TYPE *objMapContent);

#endif // !defined(AFX_SERVERHELPER_H__INCLUDED_)