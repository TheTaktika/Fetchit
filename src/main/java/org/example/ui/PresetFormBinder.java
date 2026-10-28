package org.example.ui;

import org.example.model.Preset;

public class PresetFormBinder {

    private final PresetFormPane form;

    public PresetFormBinder(PresetFormPane form) {
        this.form = form;
    }

    public void fill(Preset p) {
        if (p == null) return;
        form.nameField.setText(p.getName());
        form.formatBox.setValue(p.getFormat());
        form.audioFormatBox.setValue(p.getAudioFormat());
        form.audioQualitySpinner.getValueFactory().setValue(p.getAudioQuality());
        form.videoQualityBox.setValue(p.getVideoQuality());
        form.mergeFormatBox.setValue(p.getMergeOutputFormat());
        form.embedThumbnailBox.setSelected(p.isEmbedThumbnail());
        form.addMetadataBox.setSelected(p.isAddMetadata());
        form.embedChaptersBox.setSelected(p.isEmbedChapters());
        form.writeSubsBox.setSelected(p.isWriteSubtitles());
        form.embedSubsBox.setSelected(p.isEmbedSubtitles());
        form.subLangsField.setText(p.getSubtitleLanguages() == null ? "" : p.getSubtitleLanguages());
        form.fragmentsSpinner.getValueFactory().setValue(p.getConcurrentFragments());
        form.limitRateField.setText(p.getLimitRate() == null ? "" : p.getLimitRate());
        form.retriesSpinner.getValueFactory().setValue(p.getRetries());
        form.sponsorblockRemoveField.setText(p.getSponsorblockRemove() == null ? "" : p.getSponsorblockRemove());
        form.keepVideoBox.setSelected(p.isKeepVideo());
        form.noOverwritesBox.setSelected(p.isNoOverwrites());
        form.cookiesBox.setValue(p.getCookiesFromBrowser() == null ? "none" : p.getCookiesFromBrowser());
        form.splitChaptersBox.setSelected(p.isSplitChapters());
    }

    public void apply(Preset p) {
        LanguageManager lang = LanguageManager.getInstance();
        p.setName(form.nameField.getText().isBlank() ? lang.get("preset.no_name") : form.nameField.getText().trim());
        p.setFormat(form.formatBox.getValue() == null ? "audio" : form.formatBox.getValue());
        p.setAudioFormat(form.audioFormatBox.getValue());
        p.setAudioQuality(form.audioQualitySpinner.getValue());
        p.setVideoQuality(form.videoQualityBox.getValue());
        p.setMergeOutputFormat(form.mergeFormatBox.getValue());
        p.setEmbedThumbnail(form.embedThumbnailBox.isSelected());
        p.setAddMetadata(form.addMetadataBox.isSelected());
        p.setEmbedChapters(form.embedChaptersBox.isSelected());
        p.setWriteSubtitles(form.writeSubsBox.isSelected());
        p.setEmbedSubtitles(form.embedSubsBox.isSelected());
        p.setSubtitleLanguages(emptyToNull(form.subLangsField.getText()));
        p.setConcurrentFragments(form.fragmentsSpinner.getValue());
        p.setLimitRate(emptyToNull(form.limitRateField.getText()));
        p.setRetries(form.retriesSpinner.getValue());
        p.setSponsorblockRemove(emptyToNull(form.sponsorblockRemoveField.getText()));
        p.setKeepVideo(form.keepVideoBox.isSelected());
        p.setNoOverwrites(form.noOverwritesBox.isSelected());
        String selectedCookies = form.cookiesBox.getValue();
        p.setCookiesFromBrowser("none".equals(selectedCookies) ? null : selectedCookies);
        p.setSplitChapters(form.splitChaptersBox.isSelected());
    }

    private String emptyToNull(String s) {
        return (s == null || s.isBlank()) ? null : s.trim();
    }
}