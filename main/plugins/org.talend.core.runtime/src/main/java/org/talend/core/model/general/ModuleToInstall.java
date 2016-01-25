// ============================================================================
//
// Copyright (C) 2006-2016 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.core.model.general;

/**
 * created by WCHEN on 2012-9-17 Detailled comment
 * 
 */
public class ModuleToInstall {

    private String name;

    private String context;

    private String description;

    private String url_description;

    private String url_download;

    private boolean required;

    private String licenseType;

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getContext() {
        return this.context;
    }

    public void setContext(String context) {
        this.context = context;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getUrl_description() {
        return this.url_description;
    }

    public void setUrl_description(String url_description) {
        this.url_description = url_description;
    }

    public String getUrl_download() {
        return this.url_download;
    }

    public void setUrl_download(String url_download) {
        this.url_download = url_download;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    public boolean isRequired() {
        return this.required;
    }

    public String getLicenseType() {
        return this.licenseType;
    }

    public void setLicenseType(String licenseType) {
        this.licenseType = licenseType;
    }
}
