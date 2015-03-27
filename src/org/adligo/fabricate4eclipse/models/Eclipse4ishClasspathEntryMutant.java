package org.adligo.fabricate4eclipse.models;

import org.adligo.fabricate.models.common.I_Parameter;
import org.adligo.fabricate.models.dependencies.I_Dependency;
import org.adligo.fabricate.models.dependencies.I_Ide;
import org.adligo.fabricate.models.dependencies.I_ProjectDependency;

import java.util.List;

public class Eclipse4ishClasspathEntryMutant {
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
}
