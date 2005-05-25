package org.quartz.ui.web.init;
 
import org.quartz.ee.servlet.QuartzInitializerServlet;
import org.quartz.ui.web.model.DefinitionManager;
import org.quartz.ui.web.Util;
import org.apache.commons.betwixt.io.BeanReader;
import org.xml.sax.SAXException;
 
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletContext;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.beans.IntrospectionException;
 
/**
 *  Definition extends QuartInitializerServlet by calling its super methods, but also
 *  loading JobDefinitions into application context
 * @since Oct 2, 2003
 * @version $Revision$
 * @author Matthew Payne
 */
 
public class DefinitionInitializer extends QuartzInitializerServlet {
 
    public static String DEFAULT_DEFINITION_FILE = "/JobDefinitions.xml";
    
 public void init(ServletConfig cfg) throws ServletException {
  super.init(cfg);
 
  ServletContext context = cfg.getServletContext();
  String definitionPath = this.getInitParameter("definition-file");
 
  BeanReader beanReader = new BeanReader();
 
  // Configure the reader
  beanReader.getXMLIntrospector().setAttributesForPrimitives(false);
  
  if (definitionPath != null && definitionPath != "") {
   // Now we parse the xml
   try {
	// Register beans so that betwixt knows what the xml is to be converted to
 
	beanReader.registerBeanClass("JobDefinitions", DefinitionManager.class);
	File defFile = new File(definitionPath);
 	
				if (!defFile.exists())  {
					 
					 this.log("Alternate user definitions file, not specfic or does not exist.  Default resource /JobDefinitions.xml will be tried.");
							     
				     //defFile = new File(context.getRealPath("/WEB-INF/JobDefinitions.xml"));
				     this.log("Attempting to read definitions from file " + this.getClass().getResource(DEFAULT_DEFINITION_FILE).getFile());
				     
				     URL url = this.getClass().getResource(DEFAULT_DEFINITION_FILE);
				     
				     if (url == null) {
				         this.log("resource " + DEFAULT_DEFINITION_FILE + " not found");
				     }
				     
				     defFile = new File(url.getFile());

				}  else {
					 this.log("Reading definitions from " + definitionPath);
				}
				
	
				DefinitionManager defs = (DefinitionManager) beanReader.parse(defFile);

	if (defs!=null) {
	 context.setAttribute(Util.JOB_DEFINITIONS_PROP,  defs);
	 log(defs.getDefinitions().size() + " Definition(s) loaded from config file");
	} else {
	 log("no definitions found");
     
	}
 
   } catch (IntrospectionException e) {
   		log("error reading definitions", e);
    
   } catch (IOException e) {
   		log("IO error reading definitions", e);
	   
   } catch (SAXException e) {
   		log("error reading definitions", e);
	      }
  } else {
	log("Error definition-file init parameter not specified");
  }
 
 }
 
 public void destroy() {
  this.getServletContext().setAttribute("Util.JOB_DEFINITIONS_PROP", null);
  super.destroy();
 
 }
 
}