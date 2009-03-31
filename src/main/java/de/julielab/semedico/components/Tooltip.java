package de.julielab.semedico.components;

import org.apache.tapestry5.Asset;
import org.apache.tapestry5.ComponentResources;
import org.apache.tapestry5.MarkupWriter;
import org.apache.tapestry5.RenderSupport;
import org.apache.tapestry5.annotations.AfterRender;
import org.apache.tapestry5.annotations.Environmental;
import org.apache.tapestry5.annotations.Parameter;
import org.apache.tapestry5.annotations.Path;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.ioc.annotations.Inject;

public class Tooltip {

  @Inject @Path("wz_tooltip.js")
  private Asset wzTooltip;
  
  @Environmental
  private RenderSupport renderSupport;
  
  @Inject
  private ComponentResources resources;
  
  private String clientId;
  
  @Property
  @Parameter
  private boolean useQuestionMark;

  @Property
  @Parameter  
  private String title;
  
  @Property
  @Parameter  
  private String firstParagraph;

  @Property
  @Parameter  
  private String secondParagraph;
  
  @AfterRender
  void addJavaScript(MarkupWriter markupWriter){
    renderSupport.addScriptLink(wzTooltip);
  }
  
  
  
  void beginRender(MarkupWriter writer)
  {
      clientId = renderSupport.allocateClientId(resources);
      String output = "";
      if (useQuestionMark) {
    	  output = "<img src=\"images/ico_question_mark.gif\"/>&nbsp;";
      }
      if (title != null) {
    	  output += "<b>"+title+"</b><br/>";
      } 
      if (firstParagraph != null) {
    	  output += firstParagraph+"<br/>";
      }
      if (secondParagraph != null) {
    	  output += secondParagraph+"<br/>";
      }
 
      writer.element("span", "id", clientId, "onmouseover", "Tip('"+output+"')", "onmouseout", "Untip()", "onclick", "Untip()");      
 	  
	  /*
	   * TagToTip() doesn't work with tapestry because invisible tags will be made visible after actionLink events
	   * 
      clientId = renderSupport.allocateClientId(resources);
      writer.element("span", "id", clientId + "-1");
      if (useQuestionMark)
        writer.writeRaw("<img src=\"images/ico_question_mark.gif\"/>&nbsp;");
      if (title != null){
        writer.element("b");
        writer.writeRaw(title);
        writer.end();
        writer.element("br");
        writer.end();
      } 
      if (firstParagraph != null){
        writer.writeRaw(firstParagraph);
        writer.element("br");
        writer.end();
      }
      
      if (secondParagraph != null){
        writer.writeRaw(secondParagraph);
      }
      
      writer.end();
      writer.element("span", "id", clientId, "onmouseover", "TagToTip('"+clientId+"-1')", "onmouseout", "Untip()", "onclick", "Untip()");      
  	*/
  }


  void afterRender(MarkupWriter writer)
  {
      writer.end();
  }


  public String getClientId() {
    return clientId;
  }

}
