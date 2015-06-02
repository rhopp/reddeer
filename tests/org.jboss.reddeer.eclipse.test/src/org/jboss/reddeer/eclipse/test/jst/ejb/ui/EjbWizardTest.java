//package org.jboss.reddeer.eclipse.test.jst.ejb.ui;
//
//import static org.junit.Assert.assertTrue;
//
//import org.jboss.reddeer.eclipse.core.resources.Project;
//import org.jboss.reddeer.eclipse.jdt.ui.packageexplorer.PackageExplorer;
//import org.jboss.reddeer.eclipse.jst.ejb.ui.EjbProjectFirstPage;
//import org.jboss.reddeer.eclipse.jst.ejb.ui.EjbProjectWizard;
//import org.jboss.reddeer.eclipse.ui.perspectives.JavaEEPerspective;
//import org.jboss.reddeer.eclipse.utils.DeleteUtils;
//import org.jboss.reddeer.junit.runner.RedDeerSuite;
//import org.jboss.reddeer.requirements.openperspective.OpenPerspectiveRequirement.OpenPerspective;
//import org.junit.After;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//
//@RunWith(RedDeerSuite.class)
//@OpenPerspective(JavaEEPerspective.class)
//public class EjbWizardTest {
//	
//	@After
//	public void delete(){
//		PackageExplorer pe = new PackageExplorer();
//		pe.open();
//		for(Project p: pe.getProjects()){
//			DeleteUtils.forceProjectDeletion(p,true);
//		}
//	}
//    
//    @Test
//    public void createEJBProject(){
//        EjbProjectWizard ejb = new EjbProjectWizard();
//        ejb.open();
//        EjbProjectFirstPage firstPage = new EjbProjectFirstPage();
//        firstPage.setProjectName("ejbProject");
//        ejb.finish();
//        PackageExplorer pe = new PackageExplorer();
//        pe.open();
//        assertTrue(pe.containsProject("ejbProject"));
//    }
//
//}
