package org.adligo.fabricate4eclipse.models;

import org.adligo.fabricate.models.common.I_Parameter;
import org.adligo.fabricate.models.dependencies.I_Dependency;
import org.adligo.fabricate.models.dependencies.I_ProjectDependency;

public class Eclipse4ishClasspathEntryMutant {
  
  public static boolean is4Eclipse(I_Dependency dep) {
    String type = dep.getType();
    if ("jar".equals(type)) {
      return true;
    } else {
      return false;
    }
  }
  
  private String kind_;
  private String path_;
  private String combineaccessrules_;
  
  public Eclipse4ishClasspathEntryMutant() {}
  
  /**
   * @param dep
   * @param local file system dependent path
   * may have a eclipse environment variable prefix i.e.;
   * FAB_REPO/com.google.gwt/gwt-user-2.5.0.jar
   * or the entire path.
   */
  public Eclipse4ishClasspathEntryMutant(I_Dependency dep, String path) {
    path_ = path;
    String type = dep.getType();
    if ("jar".equals(type)) {
      kind_ = "var";
    }  else {
      throw new IllegalArgumentException("type");
    }
  }
  
  public Eclipse4ishClasspathEntryMutant(I_Parameter param) {
    kind_ = param.getKey();
    path_ = param.getValue();
  }
  
  public Eclipse4ishClasspathEntryMutant(I_ProjectDependency dep) {
    combineaccessrules_ = "false";
    kind_ = "src";
    path_ = "/" + dep.getProjectName();
    
  }
  //private Map<String,String> 
  public String getKind() {
    return kind_;
  }
  public String getPath() {
    return path_;
  }
  public String getCombineaccessrules() {
    return combineaccessrules_;
  }
  public void setKind(String kind) {
    this.kind_ = kind;
  }
  public void setPath(String path) {
    this.path_ = path;
  }
  public void setCombineaccessrules(String combineaccessrules) {
    this.combineaccessrules_ = combineaccessrules;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((combineaccessrules_ == null) ? 0 : combineaccessrules_.hashCode());
    result = prime * result + ((kind_ == null) ? 0 : kind_.hashCode());
    result = prime * result + ((path_ == null) ? 0 : path_.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    Eclipse4ishClasspathEntryMutant other = (Eclipse4ishClasspathEntryMutant) obj;
    if (combineaccessrules_ == null) {
      if (other.combineaccessrules_ != null)
        return false;
    } else if (!combineaccessrules_.equals(other.combineaccessrules_))
      return false;
    if (kind_ == null) {
      if (other.kind_ != null)
        return false;
    } else if (!kind_.equals(other.kind_))
      return false;
    if (path_ == null) {
      if (other.path_ != null)
        return false;
    } else if (!path_.equals(other.path_))
      return false;
    return true;
  }

  @Override
  public String toString() {
    return "Eclipse4ishClasspathEntryMutant [kind=" + kind_ + ", path=" + path_
        + ", combineaccessrules=" + combineaccessrules_ + "]";
  }
}
