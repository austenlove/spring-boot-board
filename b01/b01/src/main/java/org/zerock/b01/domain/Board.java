package org.zerock.b01.domain;

import lombok.*;
import org.hibernate.annotations.BatchSize;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString(exclude = "imageSet")
public class Board extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long bno;

    @Column(length = 500, nullable = false)
    private String title;

    @Column(length = 2000, nullable = false)
    private String content;

    @Column(length = 50, nullable = false)
    private String writer;

    // update 제목/내용 변경사항 반영
    public void change(String title, String content) {
        this.title = title;
        this.content = content;
    }

    // Board 엔티티 객체의 모든 상태변화에 BoardImage 객체들 역시 같이 변경 : CascadeType.ALL
    @OneToMany (mappedBy = "board", cascade = {CascadeType.ALL}, fetch = FetchType.LAZY, orphanRemoval = true)
    @Builder.Default
    @BatchSize(size=20)
    private Set<BoardImage> imageSet = new HashSet<>();


    // 이미지 추가
    public void addImage(String uuid, String fileName) {

        BoardImage boardImage = BoardImage.builder()
                .uuid(uuid)
                .fileName(fileName)
                .board(this)
                .ord(imageSet.size())
                .build();
        imageSet.add(boardImage);
    }

    // 첨부파일 모두 삭제
    public void clearImages() {
        imageSet.forEach(boardImage -> boardImage.changeBoard(null));
        this.imageSet.clear();;
    }

}
