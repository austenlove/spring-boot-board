package org.zerock.b01.dto.upload;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UploadResultDTO {

    private String uuid;
    private String fileName;
    private boolean img;   // 이미지 여부

    public String getLink() {

        if(img) {
            // 이미지인 경우 썸네일 처리
            return "s_" + uuid + "_" + fileName;
        } else {
            return uuid + "_" + fileName;
        }
    }

}
