package com.tm2ref.skin;

import java.io.Serializable;


import java.util.Objects;
import jakarta.persistence.Transient;


public class Skin implements Serializable
{
    @Transient
    private static final long serialVersionUID = 1L;

    private int skinId = 1;

    private int orgId = 0;

    private String name = "Default";

    private String baseDirectory = "default";

    private String title = "Automated Reference Checks";

    private String defaultLocaleStr = "en";

    private String template = "template.xhtml";

    private int isDefault = 1;


    public String toString()
    {
        return "Skin{" + "skinId=" + skinId + ", orgId=" + orgId + ", name=" + name + ", baseDirectory=" + baseDirectory + '}';
    }

    public int hashCode() {
        int hash = 3;
        hash = 53 * hash + this.skinId;
        hash = 53 * hash + Objects.hashCode(this.name);
        return hash;
    }


    public String getBaseDirectory() {
        return baseDirectory;
    }

    public void setBaseDirectory(String baseDirectory) {
        this.baseDirectory = baseDirectory;
    }

    public String getDefaultLocaleStr() {
        return defaultLocaleStr;
    }

    public void setDefaultLocaleStr(String defaultLocaleStr) {
        this.defaultLocaleStr = defaultLocaleStr;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getOrgId() {
        return orgId;
    }

    public void setOrgId(int orgId) {
        this.orgId = orgId;
    }

    public int getSkinId() {
        return skinId;
    }

    public void setSkinId(int skinId) {
        this.skinId = skinId;
    }

    public String getTemplate() {
        return template;
    }

    public void setTemplate(String template) {
        this.template = template;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getIsDefault() {
        return isDefault;
    }

    public void setIsDefault(int isDefault) {
        this.isDefault = isDefault;
    }
}
