package com.ranze.simpledownloaddemo.core;

import com.ranze.simpledownloaddemo.util.MD5Util;

/**
 * Created by ranze on 2018/1/16.
 */

public class Task {
    private String tag;
    private String url;
    private String savedPath;
    private String fileName;

    private Task(Builder builder) {
        this.url = builder.url;
        this.savedPath = builder.savedPath;
        this.fileName = builder.fileName;
        this.tag = MD5Util.encrypByMd5(this.url);
    }

    public String tag() {
        return tag;
    }

    public String url() {
        return url;
    }

    public String savedPath() {
        return savedPath;
    }

    public String fileName() {
        return fileName;
    }

    public static class Builder {
        private String url;
        private String savedPath;
        private String fileName;

        public Builder url(String url) {
            this.url = url;
            return this;
        }

        public Builder savedPath(String savedPath) {
            this.savedPath = savedPath;
            return this;
        }

        public Builder fileName(String fileName) {
            this.fileName = fileName;
            return this;
        }

        public Task build() {
            return new Task(this);
        }

    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null) {
            return false;
        }

        if (getClass() != obj.getClass()) {
            return false;
        } else {
            Task task = (Task) obj;
            return task.tag.equals(this.tag);
        }

    }

    @Override
    public int hashCode() {
        return tag.hashCode();
    }

    @Override
    public String toString() {
        return "Task{" +
                "tag='" + tag + '\'' +
                ", url='" + url + '\'' +
                ", savedPath='" + savedPath + '\'' +
                ", fileName='" + fileName + '\'' +
                '}';
    }
}
