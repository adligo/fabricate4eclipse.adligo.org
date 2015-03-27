package org.adligo.fabricate4eclipse.classpath_conversion;

import org.adligo.fabricate.common.util.StringUtils;
import org.adligo.fabricate.models.common.AttributesOverlay;
import org.adligo.fabricate.models.common.FabricationMemoryConstants;
import org.adligo.fabricate.models.common.FabricationRoutineCreationException;
import org.adligo.fabricate.models.common.I_FabricationMemory;
import org.adligo.fabricate.models.common.I_FabricationMemoryMutant;
import org.adligo.fabricate.models.common.I_FabricationRoutine;
import org.adligo.fabricate.models.common.I_Parameter;
import org.adligo.fabricate.models.common.I_RoutineMemory;
import org.adligo.fabricate.models.common.I_RoutineMemoryMutant;
import org.adligo.fabricate.models.dependencies.I_Dependency;
import org.adligo.fabricate.models.dependencies.I_Ide;
import org.adligo.fabricate.models.dependencies.I_ProjectDependency;
import org.adligo.fabricate.repository.I_RepositoryPathBuilder;
import org.adligo.fabricate.routines.I_FabricateAware;
import org.adligo.fabricate.routines.I_OutputProducer;
import org.adligo.fabricate.routines.I_ProjectAware;
import org.adligo.fabricate.routines.implicit.FindSrcTrait;
import org.adligo.fabricate.routines.implicit.ProjectAwareRoutine;
import org.adligo.fabricate4eclipse.models.Eclipse4ishClasspathEntryMutant;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * This routine is intended to be used as a task under a
 * ProjectQueueRoutine
 * @author scott
 *
 */
public class FabricateClasspathToEclipse4ishConverter extends ProjectAwareRoutine
  implements I_FabricationRoutine, I_ProjectAware, I_FabricateAware {
  /**
   * optional command line parameter which
   * tells this class to use the value as the eclipse 
   * environment variable, instead of full class paths.
   */
  public static final String ECLIPSE_ENV_VAR = "eclipseEnvVariable";
  private I_FabricationRoutine findSrcDirs_;
  private String eclipseEnvVar_;
  private List<String> platforms_;
  
  @SuppressWarnings("unchecked")
  @Override
  public void run() {
    super.setRunning();
    List<Eclipse4ishClasspathEntryMutant> eclipseEntries = new ArrayList<Eclipse4ishClasspathEntryMutant>();
    AttributesOverlay ao = new AttributesOverlay(fabricate_, project_);
    
    ((I_ProjectAware) findSrcDirs_).setProject(project_);
    findSrcDirs_.run();
    List<String> srcDirs = ((I_OutputProducer<List<String>>) findSrcDirs_).getOutput();
    
    for (String srcDir: srcDirs) {
      File dir = files_.instance(srcDir);
      String relDir = dir.getName();
      Eclipse4ishClasspathEntryMutant toAdd = new Eclipse4ishClasspathEntryMutant();
      toAdd.setKind("src");
      toAdd.setPath(relDir);
      if (!eclipseEntries.contains(toAdd)) {
        eclipseEntries.add(toAdd);
      }
    }
    
    List<I_Parameter> ides = ao.getAttributes(attribConstants_.getIde());
    for (I_Parameter ide: ides) {
      if ("eclipse".equalsIgnoreCase(ide.getValue())) {
        List<I_Parameter> children = ide.getChildren();
        for (I_Parameter child: children) {
          Eclipse4ishClasspathEntryMutant toAdd = new Eclipse4ishClasspathEntryMutant(child);
          if (!eclipseEntries.contains(toAdd)) {
            eclipseEntries.add(toAdd);
          }
        }
      }
    }
    List<I_Dependency> deps = project_.getNormalizedDependencies();
    String localRepoPath = fabricate_.getFabricateRepository();
    I_RepositoryPathBuilder localRepo = repositoryFactory_.createRepositoryPathBuilder(localRepoPath);
    for (I_Dependency dep: deps) {
      
      List<I_Ide> children = new ArrayList<I_Ide>(dep.getChildren());
      Iterator<I_Ide> cit = children.iterator();
      boolean addFromDep = false;
      String platform = dep.getPlatform();
      if (platform == null) {
        platform = "jse";
      } else {
        platform = platform.toLowerCase();
      }
      if (Eclipse4ishClasspathEntryMutant.is4Eclipse(dep) && platforms_.contains(platform)) {
        addFromDep = true;
      }
      while (cit.hasNext()) {
        I_Ide ide = cit.next();
        if ("eclipse".equalsIgnoreCase(ide.getName())) {
          List<I_Parameter> ideChildren = ide.getChildren();
          if (ideChildren.size() >= 1) {
            addFromDep = false;
          }
          for (I_Parameter child: ideChildren) {
            Eclipse4ishClasspathEntryMutant toAdd = new Eclipse4ishClasspathEntryMutant(child);
            if (!eclipseEntries.contains(toAdd)) {
              eclipseEntries.add(toAdd);
            }
          }
        }
      }
      if (addFromDep) {
        String path = localRepo.getArtifactPath(dep);
        if (StringUtils.isEmpty(eclipseEnvVar_)) {
          Eclipse4ishClasspathEntryMutant toAdd = new Eclipse4ishClasspathEntryMutant(dep, path);
          if (!eclipseEntries.contains(toAdd)) {
            eclipseEntries.add(toAdd);
          }
        } else {
          if (path != null) {
            path = eclipseEnvVar_ + path.substring(localRepoPath.length(), path.length());
          }
          Eclipse4ishClasspathEntryMutant toAdd = new Eclipse4ishClasspathEntryMutant(dep, path);
          if (!eclipseEntries.contains(toAdd)) {
            eclipseEntries.add(toAdd);
          }
        }
      }
    }
    
    List<I_ProjectDependency> pdeps = project_.getProjectDependencies();
    for (I_ProjectDependency pd: pdeps) {
      Eclipse4ishClasspathEntryMutant toAdd = new Eclipse4ishClasspathEntryMutant(pd);
      if (!eclipseEntries.contains(toAdd)) {
        eclipseEntries.add(toAdd);
      }
    }
    
    StringBuilder sb = new StringBuilder();
    sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
    sb.append(system_.lineSeparator());
    sb.append("<classpath>");
    sb.append(system_.lineSeparator());
    
    for (Eclipse4ishClasspathEntryMutant e: eclipseEntries) {
      sb.append("\t<classpathentry ");
      if (e.getCombineaccessrules() != null) {
        sb.append("combineaccessrules=\"");
        sb.append(e.getCombineaccessrules());
        sb.append("\" ");
      }
      
      sb.append("kind=\"");
      sb.append(e.getKind());
      sb.append("\" ");
      
      sb.append("path=\"");
      sb.append(e.getPath());
      sb.append("\"/>");
      sb.append(system_.lineSeparator());
      
    }
    sb.append("</classpath>");
    sb.append(system_.lineSeparator());
    
    String classpathFile = project_.getDir() + ".classpath";
    if (files_.exists(classpathFile)) {
      try {
        files_.delete(classpathFile);
      } catch (IOException e1) {
        //pass to run monitor
        throw new RuntimeException(e1);
      }
    }
    //files_.create(classpathFile);
    try {
      OutputStream out = files_.newFileOutputStream(classpathFile);
      files_.writeFile(new ByteArrayInputStream(sb.toString().getBytes("UTF-8")), out);
    } catch (IOException e1) {
      //pass to run monitor
      throw new RuntimeException(e1);
    }
  }

  
  @SuppressWarnings("unchecked")
  @Override
  public boolean setupInitial(I_FabricationMemoryMutant<Object> memory,
      I_RoutineMemoryMutant<Object> routineMemory) throws FabricationRoutineCreationException {
    
    platforms_ = new ArrayList<String>(
        (List<String>)
        memory.get(FabricationMemoryConstants.PLATFORMS));
    findSrcDirs_ = createFindSrcTrait();
    if (!findSrcDirs_.setupInitial(memory, routineMemory)) {
      return false;
    }
    setEclipseEnvVar();
    return super.setupInitial(memory, routineMemory);
  }


  @SuppressWarnings("unchecked")
  @Override
  public void setup(I_FabricationMemory<Object> memory, I_RoutineMemory<Object> routineMemory)
      throws FabricationRoutineCreationException {
    
    platforms_ = new ArrayList<String>(
        (List<String>)
        memory.get(FabricationMemoryConstants.PLATFORMS));
    
    findSrcDirs_ = createFindSrcTrait();
    findSrcDirs_.setup(memory, routineMemory);
    setEclipseEnvVar();
    super.setup(memory, routineMemory);
  }
  
  private I_FabricationRoutine createFindSrcTrait() throws FabricationRoutineCreationException {
    I_FabricationRoutine ret = traitFactory_.createRoutine(FindSrcTrait.NAME, FindSrcTrait.IMPLEMENTED_INTERFACES);
    ret.setSystem(system_);
    ((I_FabricateAware) ret).setFabricate(fabricate_);
    return ret;
  }
  
  private void setEclipseEnvVar() {
    String value = system_.getArgValue(ECLIPSE_ENV_VAR);
    if (!StringUtils.isEmpty(value)) {
      eclipseEnvVar_ = value;
    }
  }
}
