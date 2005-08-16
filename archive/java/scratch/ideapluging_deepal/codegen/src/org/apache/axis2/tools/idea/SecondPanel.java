package org.apache.axis2.tools.idea;

import org.apache.axis2.tools.bean.CodegenBean;

import javax.swing.*;
import java.awt.*;

import org.apache.axis2.wsdl.codegen.CommandLineOptionConstants;
import org.apache.axis2.tools.bean.CodegenBean;

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
*
*
*/

/**
 * Author : Deepal Jayasinghe
 * Date: Jul 20, 2005
 * Time: 10:52:36 PM
 */
public class SecondPanel extends JPanel {
    JLabel lblol;
    JComboBox comlanguage;
    ButtonGroup cbg;
    JRadioButton rdsynasyn;
    JRadioButton rdsyn;
    JRadioButton rdasync;
    JLabel lblpakage;
    JTextField txtpackage;
    private CodegenBean codegenBean;

    public SecondPanel(CodegenBean codegenBean) {
        this.codegenBean = codegenBean;
        SecondPanelLayout customLayout = new SecondPanelLayout();
        setFont(new Font("Helvetica", Font.PLAIN, 12));
        setLayout(customLayout);

        lblol = new JLabel("   Select the output language");
        add(lblol);

        comlanguage = new JComboBox();
        comlanguage.addItem("Java");
        comlanguage.addItem("C#");
        comlanguage.addItem("C++");
        add(comlanguage);

        cbg = new ButtonGroup();
        rdsynasyn = new JRadioButton(" Generate both sync and async", true);
        cbg.add(rdsynasyn);
        add(rdsynasyn);

        rdsyn = new JRadioButton(" Generate sync only", false);
        cbg.add(rdsyn);
        add(rdsyn);

        rdasync = new JRadioButton(" Generate async only", false);
        cbg.add(rdasync);
        add(rdasync);

        lblpakage = new JLabel("Set the package name");
        add(lblpakage);

        txtpackage = new JTextField("org.axis2");
        add(txtpackage);

        setSize(getPreferredSize());

    }

    public void fillBean(){
        int index =comlanguage.getSelectedIndex();
        switch(index ){
            case 0: {
                codegenBean.setLanguage(CommandLineOptionConstants.LanguageNames.JAVA);
                break;
            } case 1: {
                codegenBean.setLanguage(CommandLineOptionConstants.LanguageNames.C_SHARP);
                break;
            } case 2 : {
                codegenBean.setLanguage(CommandLineOptionConstants.LanguageNames.C_PLUS_PLUS);
                break;
            }
        }
        if(rdasync.isSelected()) {
            codegenBean.setAsyncOnly(true);
        } else if (rdsyn.isSelected()){
            codegenBean.setSyncOnly(true);
        } else {
            codegenBean.setSyncOnly(false);
            codegenBean.setAsyncOnly(false);
        }
        codegenBean.setPackageName(txtpackage.getText());
    }
}

class SecondPanelLayout implements LayoutManager {

    public SecondPanelLayout() {
    }

    public void addLayoutComponent(String name, Component comp) {
    }

    public void removeLayoutComponent(Component comp) {
    }

    public Dimension preferredLayoutSize(Container parent) {
        Dimension dim = new Dimension(0, 0);

        Insets insets = parent.getInsets();
        dim.width = 565 + insets.left + insets.right;
        dim.height = 235 + insets.top + insets.bottom;

        return dim;
    }

    public Dimension minimumLayoutSize(Container parent) {
        Dimension dim = new Dimension(0, 0);
        return dim;
    }

    public void layoutContainer(Container parent) {
        Insets insets = parent.getInsets();

        Component c;
        c = parent.getComponent(0);
        if (c.isVisible()) {c.setBounds(insets.left+0,insets.top+5,200,20);}
        c = parent.getComponent(1);
        if (c.isVisible()) {c.setBounds(insets.left+210,insets.top+5,160,20);}
        c = parent.getComponent(2);
        if (c.isVisible()) {c.setBounds(insets.left+8,insets.top+30,250,24);}
        c = parent.getComponent(3);
        if (c.isVisible()) {c.setBounds(insets.left+8,insets.top+55,150,24);}
        c = parent.getComponent(4);
        if (c.isVisible()) {c.setBounds(insets.left+8,insets.top+80,150,24);}
        c = parent.getComponent(5);
        if (c.isVisible()) {c.setBounds(insets.left+8,insets.top+110,200,24);}
        c = parent.getComponent(6);
        if (c.isVisible()) {c.setBounds(insets.left+210,insets.top+110,200,24);}
    }
}

