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
 */

// WSDDHandler.h: 
//
//////////////////////////////////////////////////////////////////////
#ifdef WIN32
#pragma warning (disable : 4786)
#endif

#if !defined(AFX_WSDDHANDLER_H__51DC7642_033D_443B_9D97_5825C38B23DB__INCLUDED_)
#define AFX_WSDDHANDLER_H__51DC7642_033D_443B_9D97_5825C38B23DB__INCLUDED_

#include "../common/GDefine.h"
#include <string>
#include <map>
#include <list>

using namespace std;

enum AXIS_HANDLER_SCOPE {AH_APPLICATION=1, AH_SESSION, AH_REQUEST};

const AxisChar kw_scope_app[] = "application";
const AxisChar kw_scope_ses[] = "session";
const AxisChar kw_scope_req[] = "request";


/**
    @class WSDDHandler
    @brief interface for the WSDDHandler class.


    @author Susantha Kumara (skumara@virtusa.com)
*/
class WSDDHandler  
{
public:
	const AxisChar* GetLibName() const;
	int GetLibId() const;
	int GetScope() const;
	void SetScope(const AxisChar* sScope);
	void SetLibName(const AxisChar* sLibName);
	void SetLibId(int nLibId);
	const AxisChar* GetParameter(const AxisChar* sKey) const;
	void AddParameter(const AxisChar* sKey, const AxisChar* sValue);
	const map<AxisString, AxisString>* GetParameterList() const; 
	WSDDHandler();
	virtual ~WSDDHandler();
	void SetDescription(const AxisChar* sDescription);
	const AxisChar* GetDescription() const;

protected:
	int m_nLibId;
	int m_nScope;
	AxisString m_sName;
	AxisString m_sLibName;
	AxisString m_sDescription;
	map<AxisString, AxisString>* m_Params;
};

typedef list<WSDDHandler*> WSDDHandlerList;

#endif // !defined(AFX_WSDDHANDLER_H__51DC7642_033D_443B_9D97_5825C38B23DB__INCLUDED_)