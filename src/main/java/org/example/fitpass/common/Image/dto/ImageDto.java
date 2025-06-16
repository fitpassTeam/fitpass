package org.example.fitpass.common.Image.dto;

import org.example.fitpass.common.Image.entity.Image;

public record ImageDto(
    Long id,
    String url
) {
    public static ImageDto from(Image image) {
        return new ImageDto(image.getId(), image.getUrl());
    }
}
