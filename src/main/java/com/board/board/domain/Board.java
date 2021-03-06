package com.board.board.domain;

import com.board.board.domain.oauth.User;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Entity // DB와의 연결을 위하여
@Data   // getter setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
//@Where(clause = "is_deleted = true")
//@SQLDelete(sql = "UPDATE board SET is_deleted = true WHERE id = ?")
public class Board {
    @Id // id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    private String title;

    @Lob
    @Column(columnDefinition="TEXT", nullable = false)
    private String content;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="user_id", referencedColumnName = "id")
    private User user;

    private boolean isDeleted = Boolean.FALSE;

    // 삭제 CASCADE를 위함
    @OneToMany(mappedBy = "board", cascade = CascadeType.ALL)
    private List<Reply> replys = new ArrayList<>();

//    @OneToMany(mappedBy = "board", cascade = CascadeType.ALL)
//    private List<Heart> hearts = new ArrayList<>();

    public Board(Long id, String title, String content, User user) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.user = user;
    }
}
