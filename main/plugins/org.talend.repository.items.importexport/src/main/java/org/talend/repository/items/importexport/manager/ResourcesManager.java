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
package org.talend.repository.items.importexport.manager;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.talend.repository.items.importexport.handlers.model.ImportItem;

/**
 */
public abstract class ResourcesManager {

    protected Map<IPath, Object> path2Object = new HashMap<IPath, Object>();

    public void add(String path, Object object) {
        path2Object.put(new Path(path), object);
    }

    public void add(IPath path, Object object) {
        path2Object.put(path, object);
    }

    public Set<IPath> getPaths() {
        return path2Object.keySet();
    }

    public boolean isContainsPath(IPath path) {
        return getPaths().contains(path);
    }

    final public InputStream getStream(IPath path) throws IOException {
        return getStream(path, null);
    }

    public abstract InputStream getStream(IPath path, ImportItem importItem) throws IOException;

    public abstract boolean collectPath2Object(Object root);

    public abstract void closeResource();

    public abstract Object getRoot();
}
