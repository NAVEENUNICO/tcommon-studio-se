// ============================================================================
//
// Copyright (C) 2006-2012 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.resource;

import org.talend.core.IService;

/**
 * DOC ycbai class global comment. Detailled comment
 */
public interface IExportJobResourcesService extends IService {

    /**
     * DOC ycbai Comment method "getAntScriptFilePath".
     * 
     * Get the path of ant script template file.
     * 
     * @return
     */
    public String getAntScriptFilePath();

    /**
     * DOC ycbai Comment method "getMavenScriptFilePath".
     * 
     * Get the path of maven script template file.
     * 
     * @return
     */
    public String getMavenScriptFilePath(String file);

    /**
     * DOC ycbai Comment method "getScriptFromPreferenceStore".
     * 
     * @param type the constant from IExportJobPrefConstants.
     * @return the script string from preference store.
     */
    public String getScriptFromPreferenceStore(String type);

}
