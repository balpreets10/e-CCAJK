package com.mycca.models;

import android.net.Uri;

import java.io.File;

public class SelectedImageModel {

    private Uri imageURI;
    private File file;
    private String selectedImageName;

    public SelectedImageModel(Uri imageURI) {
        this.imageURI = imageURI;
        file = new File(imageURI.getPath());
    }

    public SelectedImageModel(File file) {
        this.file = file;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public String getSelectedImageName() {
        return selectedImageName;
    }

    public void setSelectedImageName(String selectedImageName) {
        this.selectedImageName = selectedImageName;
    }

    public Uri getImageURI() {
        return imageURI;
    }

    public void setImageURI(Uri imageURI) {
        this.imageURI = imageURI;
    }

}