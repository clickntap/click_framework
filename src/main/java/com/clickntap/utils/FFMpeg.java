package com.clickntap.utils;

import java.io.ByteArrayOutputStream;
import java.io.File;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.PumpStreamHandler;
import org.json.JSONArray;
import org.json.JSONObject;

public class FFMpeg {

  private String path;

  public FFMpeg() {
    super();
    this.path = ConstUtils.EMPTY;
  }

  public FFMpeg(String path) {
    super();
    this.path = path;
  }

  public String getPath() {
    return path;
  }

  public void setPath(String path) {
    this.path = path;
  }

  public static void main(String args[]) throws Exception {
    long lo = System.currentTimeMillis();
    FFMpeg ffmpeg = new FFMpeg();
    System.out.println(ffmpeg.getVideoInfo(new File("etc/mov_bbb.mp4")).toString(2));
    System.out.println((System.currentTimeMillis() - lo) + " millis");
  }

  public JSONObject getVideoInfo(File source) throws Exception {
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    CommandLine cmdl = new CommandLine(path + "ffprobe");
    cmdl.addArgument("-v");
    cmdl.addArgument("quiet");
    cmdl.addArgument("-print_format");
    cmdl.addArgument("json");
    cmdl.addArgument("-show_format");
    cmdl.addArgument("-show_streams");
    cmdl.addArgument(source.getCanonicalPath());
    DefaultExecutor executor = new DefaultExecutor();
    PumpStreamHandler streamHandler = new PumpStreamHandler(outputStream);
    executor.setStreamHandler(streamHandler);
    executor.execute(cmdl);
    return new JSONObject(outputStream.toString());
  }

  public File exportVideoTrack(File source) throws Exception {
    return exportTrack(source, "video");
  }

  public File exportAudioTrack(File source) throws Exception {
    return exportTrack(source, "audio");
  }

  public boolean isVideo(File source) throws Exception {
    JSONArray streams = getVideoInfo(source).getJSONArray("streams");
    for (int i = 0; i < streams.length(); i++) {
      JSONObject stream = streams.getJSONObject(i);
      if (stream.getString("codec_type").equals("video")) {
        return true;
      }
    }
    return false;
  }

  public File exportTrack(File source, String trackType) throws Exception {
    File dest = new File(source.getAbsolutePath() + "." + trackType + ".mp4");
    dest.delete();
    int trackIndex = 0;
    JSONArray streams = getVideoInfo(source).getJSONArray("streams");
    for (int i = 0; i < streams.length(); i++) {
      JSONObject stream = streams.getJSONObject(i);
      if (stream.getString("codec_type").equals(trackType)) {
        trackIndex = stream.getInt("index");
        break;
      }
    }
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    CommandLine cmdl = new CommandLine(path + "ffmpeg");
    cmdl.addArgument("-i");
    cmdl.addArgument(source.getAbsolutePath());
    cmdl.addArgument("-map");
    cmdl.addArgument("0:" + trackIndex);
    cmdl.addArgument("-" + trackType.substring(0, 1) + "codec");
    cmdl.addArgument("copy");
    cmdl.addArgument(dest.getAbsolutePath());
    DefaultExecutor executor = new DefaultExecutor();
    PumpStreamHandler streamHandler = new PumpStreamHandler(outputStream);
    executor.setStreamHandler(streamHandler);
    executor.execute(cmdl);
    return dest;
  }
}
