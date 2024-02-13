package org.zerock.b01.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PageRequestDTO {

    @Builder.Default
    private int page = 1;

    @Builder.Default
    private int size = 10;

    // 검색 종류 t, c, w, tc, tw, twc
    private String type;

    private String keyword;

    // 검색 조건을 String[] 처리
    public String[] getTypes() {

        if(type == null || type.isEmpty()) {
            return null;
        }
        return type.split("");
    }

    // Pageable 타입 반환
    public Pageable getPageable(String...props) {
        return PageRequest.of(this.page-1, this.size, Sort.by(props).descending());
    }

    // 검색 조건과 페이징 조건을 문자열로 구성
    private String link;

    public String getLink() {

        if(link == null) {
            StringBuilder builder = new StringBuilder();

            builder.append("page=" + this.page);
            builder.append("&size=" + this.size);

            if(type != null && type.length() > 0) {
                builder.append("&type=" + this.type);
            }

            if(keyword != null) {
                try {
                    builder.append("&keyword=" + URLEncoder.encode(keyword, "UTF-8"));

                } catch (UnsupportedEncodingException e) {}
            }   // if keyword end

            link = builder.toString();
        }   // if link end

        return link;
    }   // getLink end

}
