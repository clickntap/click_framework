package com.clickntap.platform;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.batik.transcoder.Transcoder;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.JPEGTranscoder;
import org.apache.batik.transcoder.image.PNGTranscoder;
import org.apache.commons.io.FileUtils;
import org.apache.fop.render.ps.EPSTranscoder;
import org.apache.fop.svg.PDFTranscoder;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;

import com.clickntap.utils.XMLUtils;

public class SVG {

	private FExecutor fExecutor;
	private String codeScript;
	private String dataScript;

	public SVG(FExecutor fExecutor, String codeScript, String dataScript) {
		this.fExecutor = fExecutor;
		this.codeScript = codeScript;
		this.dataScript = dataScript;
	}

	public SVG(FExecutor fExecutor, String codeScript) {
		this(fExecutor, codeScript, null);
	}

	public void svg(OutputStream out) throws Exception {
		svg(out, null);
	}

	public void svg(OutputStream out, Integer width) throws Exception {
		FTask task = new FTask();
		task.setCodeScript(codeScript);
		task.setDataScript(dataScript);
		fExecutor.execute(task);
		if (task.getData() != null) {
			String code = task.getCode();
			StringBuffer svg = new StringBuffer();
			if (width != null) {
				StringBuffer defs = new StringBuffer();
				File fontFile = new File(fExecutor.getApp().getWorkDir().getFile().getAbsolutePath() + "/barlow-condensed-v4-latin-ext_latin-600.svg");
				defs.append(FileUtils.readFileToString(fontFile));
				defs.append("<defs><style>text {font-family:\"Barlow Condensed SemiBold\";}</style></defs>");
				int x = code.indexOf(">");
				svg.append(code.substring(0, x + 1));
				svg.append(defs);
				svg.append(code.substring(x + 1));
			} else {
				svg.append(code);
			}
			Document doc = DocumentHelper.parseText(svg.toString());
			if (width != null) {
				doc.getRootElement().addAttribute("width", width.toString());
				doc.getRootElement().addAttribute("height", Integer.toString((int) (task.getData().getInt("h") * (((float) width) / task.getData().getInt("w")))));
			}
			XMLUtils.copyTo(doc, out);
		} else {
			Document doc = DocumentHelper.parseText(task.getCode());
			doc.getRootElement().addAttribute("width", "100%");
			doc.getRootElement().addAttribute("height", "100%");
			XMLUtils.copyTo(doc, out);
		}
	}

	public void pdf(OutputStream outputStream) throws Exception {
		PDFTranscoder t = new PDFTranscoder();
		transcode(t, 2880, outputStream);
	}

	public void eps(OutputStream outputStream) throws Exception {
		EPSTranscoder t = new EPSTranscoder();
		transcode(t, 2880, outputStream);
	}

	public void png(OutputStream outputStream) throws Exception {
		Transcoder transcoder = new PNGTranscoder();
		transcode(transcoder, 360, outputStream);
	}

	public void jpg(OutputStream outputStream) throws Exception {
		Transcoder transcoder = new JPEGTranscoder();
		transcoder.addTranscodingHint(JPEGTranscoder.KEY_QUALITY, 1.0f);
		transcode(transcoder, 2880, outputStream);
	}

	public void transcode(Transcoder transcoder, Integer width, OutputStream outputStream) {
		try {
			ByteArrayOutputStream svg = new ByteArrayOutputStream();
			svg(svg, width);
			InputStream in = new ByteArrayInputStream(svg.toByteArray());
			TranscoderInput transcoderInput = new TranscoderInput(in);
			TranscoderOutput transcoderOutput = new TranscoderOutput(outputStream);
			transcoder.transcode(transcoderInput, transcoderOutput);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
