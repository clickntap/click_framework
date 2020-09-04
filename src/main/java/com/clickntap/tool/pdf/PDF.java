package com.clickntap.tool.pdf;

import java.awt.Dimension;
import java.awt.Insets;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.Charset;

import org.springframework.core.io.Resource;

import com.clickntap.api.M;
import com.clickntap.tool.f.F;
import com.clickntap.tool.script.FreemarkerScriptEngine;
import com.clickntap.utils.ConstUtils;
import com.itextpdf.text.Document;
import com.itextpdf.text.RectangleReadOnly;
import com.itextpdf.text.pdf.PdfCopy;
import com.itextpdf.text.pdf.PdfReader;

public class PDF {

  private Resource workDir;
  private FreemarkerScriptEngine engine;
  private F f;

  public void setWorkDir(Resource workDir) {
    this.workDir = workDir;
  }

  public void setF(F f) {
    this.f = f;
  }

  public void init() throws Exception {
    engine = new FreemarkerScriptEngine();
    engine.setExtension(ConstUtils.EMPTY);
    engine.setTemplateDir(workDir);
    engine.start();
  }

  /*
   * A4 measures 210 × 297 millimeters.
   * In PostScript, its dimensions are rounded off to 595 × 842 points.
   * We will use a 1190x1684 pixels (595*2x842*2 pixels) resolution.
   */

  public void render(PDFContext ctx, String templateName, boolean portrait, OutputStream out) throws Exception {
    Number width = null;
    Number height = null;
    if (portrait) {
      width = 595;
      height = 842;
    } else {
      width = 842;
      height = 595;
    }
    render(ctx, templateName, width, height, out);
  }

  public void render(PDFContext ctx, String templateName, Number width, Number height, OutputStream out) throws Exception {
    if (f != null) {
      ctx.put("f", f);
      f.getJavascriptEngine().put("ctx", ctx);
    }
    Document pdfDocument = null;
    String html = engine.eval(ctx, templateName);
    Object pd4ml = Class.forName("org.zefer.pd4ml.PD4ML").getDeclaredConstructor().newInstance();
    M.invoke(pd4ml, "useTTF", new Object[] { workDir.getFile().getAbsolutePath(), true });
    M.invoke(pd4ml, "setPageSize", new Dimension(width.intValue(), height.intValue()));
    M.invoke(pd4ml, "setHtmlWidth", width.intValue() * 2);
    pdfDocument = new Document(new RectangleReadOnly(width.intValue(), height.intValue()));
    pdfDocument.setMarginMirroring(true);
    pdfDocument.setMargins(0, 0, 0, 0);
    M.invoke(pd4ml, "setPageInsets", new Insets(0, 0, 0, 0));
    {
      PdfCopy pdf = new PdfCopy(pdfDocument, out);
      pdfDocument.open();
      for (int i = 0; i < ctx.getNumberOfPages().intValue(); i++) {
        ctx.setPageNumber(i + 1);
        if (i != 0) {
          html = engine.eval(ctx, templateName);
        }
        OutputStream pdfOut = new ByteArrayOutputStream();
        ByteArrayInputStream in = new ByteArrayInputStream(html.getBytes(ConstUtils.UTF_8));
        InputStreamReader pdfIn = new InputStreamReader(in, Charset.forName(ConstUtils.UTF_8));
        M.invoke(pd4ml, "render", new Object[] { pdfIn, pdfOut });
        pdfIn.close();
        in.close();
        PdfReader reader = new PdfReader(((ByteArrayOutputStream) pdfOut).toByteArray());
        for (int n = 1; n <= reader.getNumberOfPages(); n++) {
          pdf.addPage(pdf.getImportedPage(reader, n));
        }
        pdfOut.close();
      }
      pdf.close();
    }
    pdfDocument.close();
  }

}
