package org.zerock.b01.repository.search;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.JPQLQuery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;
import org.zerock.b01.domain.Board;
import org.zerock.b01.domain.QBoard;
import org.zerock.b01.domain.QReply;
import org.zerock.b01.dto.*;

import java.util.List;
import java.util.stream.Collectors;

public class BoardSearchImpl extends QuerydslRepositorySupport implements BoardSearch {

    public BoardSearchImpl() {
        super(Board.class);
    }

    // pageable의 검색 결과와 전체 결과의 개수를 이용하여 Page<Board> 객체를 생성하고 반환
    @Override
    public Page<Board> search1(Pageable pageable) {

        // Q도메인 객체
        QBoard board = QBoard.board;

        // select.. from board
        JPQLQuery<Board> query = from(board);

        // 검색조건
        BooleanBuilder booleanBuilder = new BooleanBuilder();

        booleanBuilder.or(board.title.contains("11"));   // where title like..
        booleanBuilder.or(board.content.contains("11"));   // where content like...

        query.where(booleanBuilder);   // 위의 두 조건을 만족하고
        query.where(board.bno.gt(0L));   // bno가 0L보다 큰

        // paging
        this.getQuerydsl().applyPagination(pageable, query);

        List<Board> list = query.fetch();

        long count = query.fetchCount();

        return null;

    }

    @Override
    public Page<Board> searchAll(String[] types, String keyword, Pageable pageable) {

        QBoard board = QBoard.board;
        JPQLQuery<Board> query = from(board);

        if( (types!=null && types.length>0) && keyword!=null) {

            BooleanBuilder booleanBuilder = new BooleanBuilder();

            for(String type: types) {
                switch (type) {
                    case "t": booleanBuilder.or(board.title.contains(keyword));
                    break;
                    case "c": booleanBuilder.or(board.content.contains(keyword));
                    break;
                    case "w": booleanBuilder.or(board.writer.contains(keyword));
                    break;
                }
            }   // end for

            query.where(booleanBuilder);
        }   // end if

        // bno > 0
        query.where(board.bno.gt(0L));

        // paging
        this.getQuerydsl().applyPagination(pageable, query);

        List<Board> list = query.fetch();
        long count = query.fetchCount();

//        return null;
        return new PageImpl<>(list, pageable, count);

    }

    @Override
    public Page<BoardListReplyCountDTO> searchWithReplyCount(String[] types, String keyword, Pageable pageable) {

        QBoard board = QBoard.board;
        QReply reply = QReply.reply;

        JPQLQuery<Board> query = from(board);
        query.leftJoin(reply).on(reply.board.eq(board));

        query.groupBy(board);

        // 검색조건 적용
        if( (types != null && types.length>0) && keyword != null ) {

            BooleanBuilder booleanBuilder = new BooleanBuilder();

            for(String type : types) {

                switch(type) {
                    case "t":
                        booleanBuilder.or(board.title.contains(keyword));
                        break;
                    case "c":
                        booleanBuilder.or(board.content.contains(keyword));
                        break;
                    case "w":
                        booleanBuilder.or(board.writer.contains(keyword));
                        break;
                }
            }   // end for
            query.where(booleanBuilder);
        }

        // bno>0
        query.where(board.bno.gt(0L));

        JPQLQuery<BoardListReplyCountDTO> dtoQuery = query.select(Projections.
                bean(BoardListReplyCountDTO.class,
                        board.bno,
                        board.title,
                        board.writer,
                        board.regDate,
                        reply.count().as("replyCount")
                        ));

        this.getQuerydsl().applyPagination(pageable, dtoQuery);

        List<BoardListReplyCountDTO> dtoList = dtoQuery.fetch();

        long count = dtoQuery.fetchCount();

        return new PageImpl<>(dtoList, pageable, count);
    }

    // 검색 조건 추가
    @Override
    public Page<BoardListAllDTO> searchWithAll(String[] types, String keyword, Pageable pageable) {

        QBoard board = QBoard.board;
        QReply reply = QReply.reply;

        JPQLQuery<Board> boardJPQLQuery = from(board);
        boardJPQLQuery.leftJoin(reply).on(reply.board.eq(board));   // left join

        // 검색조건 적용
        if( (types != null && types.length>0) && keyword != null ) {

            BooleanBuilder booleanBuilder = new BooleanBuilder();

            for(String type : types) {

                switch(type) {
                    case "t":
                        booleanBuilder.or(board.title.contains(keyword));
                        break;
                    case "c":
                        booleanBuilder.or(board.content.contains(keyword));
                        break;
                    case "w":
                        booleanBuilder.or(board.writer.contains(keyword));
                        break;
                }
            }   // end for
            boardJPQLQuery.where(booleanBuilder);
        }

        boardJPQLQuery.groupBy(board);

        getQuerydsl().applyPagination(pageable, boardJPQLQuery);   // paging

        JPQLQuery<Tuple> tupleJPQLQuery = boardJPQLQuery.select(board, reply.countDistinct());

        List<Tuple> tupleList = tupleJPQLQuery.fetch();

        // List<Tuple> 처리
        List<BoardListAllDTO> dtoList = tupleList.stream().map(tuple -> {

            Board board1 = (Board) tuple.get(board);
            long replyCount = tuple.get(1, Long.class);

            BoardListAllDTO dto = BoardListAllDTO.builder()
                    .bno(board1.getBno())
                    .title(board1.getTitle())
                    .writer(board1.getWriter())
                    .regDate(board1.getRegDate())
                    .replyCount(replyCount)
                    .build();

            // BoardImage를 BoardImageDTO 처리할 부분
            List<BoardImageDTO> imageDTOS = board1.getImageSet().stream().sorted()
                    .map(boardImage -> BoardImageDTO.builder()
                            .uuid(boardImage.getUuid())
                            .fileName(boardImage.getFileName())
                            .ord(boardImage.getOrd())
                            .build()
                    ).collect(Collectors.toList());

            // 처리된 BoardImageDTO 추가
            dto.setBoardImages(imageDTOS);
            return dto;

        }).collect(Collectors.toList());

        long totalCount = boardJPQLQuery.fetchCount();

        return new PageImpl<>(dtoList, pageable, totalCount);
    }

}
