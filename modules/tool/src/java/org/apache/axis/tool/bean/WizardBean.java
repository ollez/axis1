package org.apache.axis.tool.bean;
/*
 * Copyright 2004,2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

public class WizardBean {
    private Page1Bean page1bean;
    private Page2Bean page2bean;
    private Page3Bean page3bean;

    public Page1Bean getPage1bean() {
        return page1bean;
    }

    public void setPage1bean(Page1Bean page1bean) {
        this.page1bean = page1bean;
    }

    public Page2Bean getPage2bean() {
        return page2bean;
    }

    public void setPage2bean(Page2Bean page2bean) {
        this.page2bean = page2bean;
    }

    public Page3Bean getPage3bean() {
        return page3bean;
    }

    public void setPage3bean(Page3Bean page3bean) {
        this.page3bean = page3bean;
    }

}
