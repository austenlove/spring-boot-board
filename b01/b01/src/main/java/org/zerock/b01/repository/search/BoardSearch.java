package org.zerock.b01.repository.search;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.zerock.b01.domain.Board;
import org.zerock.b01.dto.BoardListAllDTO;
import org.zerock.b01.dto.BoardListReplyCountDTO;

public interface BoardSearch {

    // search1은 pageable을 이용해 Page<Board> 객체 반환
    Page<Board> search1(Pageable pageable);

    // 검색 조건과 키워드 여러 조합이 가능하도록 처리하는 메소드
    Page<Board> searchAll(String[] types, String keyword, Pageable pageable);

    Page<BoardListReplyCountDTO> searchWithReplyCount(String[] types, String keyword, Pageable pageable);

    Page<BoardListAllDTO> searchWithAll(String[] types, String keyword, Pageable pageable);

}
