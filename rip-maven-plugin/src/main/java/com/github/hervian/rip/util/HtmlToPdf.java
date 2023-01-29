package com.github.hervian.rip.util;

import lombok.Builder;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.xhtmlrenderer.layout.SharedContext;
import org.xhtmlrenderer.pdf.ITextRenderer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.FileSystems;

/**
 * Generate XHTML document from html file and css file.
 * Code copy pastet from https://www.netjstech.com/2021/02/html-to-pdf-java-flying-saucer-openpdf.html
 *
 * NB This is a work in progress.
 */
@Builder
public class HtmlToPdf {

  private MavenProject project;

  public void execute() throws MojoExecutionException {
    File swaggerHtmlFile = new File(project.getBuild().getOutputDirectory()+"/swagger/swagger.html");
    swaggerHtmlFile.deleteOnExit();
    // Converted PDF file - Output
    File outputPdf = new File(project.getBuild().getOutputDirectory()+"/swagger/swagger.pdf");
    //create well formed HTML
    try {
    String xhtml = createWellFormedHtml(swaggerHtmlFile);
    System.out.println("Starting conversion to PDF...");
    xhtmlToPdf(xhtml, outputPdf);
    } catch (IOException e) {
      throw new MojoExecutionException("Unable to generate PDF", e);
    }
  }

  private String createWellFormedHtml(File inputHTML) throws IOException {
    Document document = Jsoup.parse(inputHTML, "UTF-8");
    document.outputSettings().syntax(Document.OutputSettings.Syntax.xml);
    System.out.println("HTML parsing done...");
    return document.html();
  }

  private void xhtmlToPdf(String xhtml, File outputPdf) throws IOException {
    OutputStream outputStream = null;
    try {
      ITextRenderer renderer = new ITextRenderer();
      SharedContext sharedContext = renderer.getSharedContext();
      sharedContext.setPrint(true);
      sharedContext.setInteractive(false);
      // Register custom ReplacedElementFactory implementation
      sharedContext.getTextRenderer().setSmoothingThreshold(0);
      // Register additional font
      //renderer.getFontResolver().addFont(getClass().getClassLoader().getResource("fonts/PRISTINA.ttf").toString(), true);
      // Setting base URL to resolve the relative URLs

      //TODO: Edit below. work in progress
      String baseUrl = FileSystems.getDefault()
        .getPath("F:\\", "Anshu\\NetJs\\Programs\\", "src\\main\\resources\\css")
        .toUri()
        .toURL()
        .toString();
      renderer.setDocumentFromString(xhtml, baseUrl);
      renderer.layout();
      outputStream = new FileOutputStream(outputPdf);
      renderer.createPDF(outputStream);
      System.out.println("PDF creation completed");
    }finally {
      if(outputStream != null)
        outputStream.close();
    }
  }
}
