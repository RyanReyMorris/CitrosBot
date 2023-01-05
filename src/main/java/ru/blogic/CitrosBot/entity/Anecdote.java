package ru.blogic.CitrosBot.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

/**
 * Сущность анекдота пользователя
 *
 * @author eyakimov
 */
@Entity(name = "Anecdote")
@Table(name = "anecdote")
@Getter
@ToString
@RequiredArgsConstructor
public class Anecdote {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.REFRESH)
    @JoinColumn(name = "author_id", referencedColumnName = "id")
    private UserEntity author;

    @Column(name = "file_id")
    private String fileId;

    @Column(name = "name")
    private String name;

    @Column(name = "file_type")
    private String fileType;

    public void changeFileId(String fileId) {
        this.fileId = fileId;
    }

    public void changeFileType(String fileType) {
        this.fileType = fileType;
    }

    public static Anecdote.AnecdoteBuilder newBuilder() {
        return new Anecdote().new AnecdoteBuilder();
    }

    public class AnecdoteBuilder {
        private AnecdoteBuilder() {
        }

        public Anecdote.AnecdoteBuilder setId(Long id) {
            Anecdote.this.id = id;
            return this;
        }

        public Anecdote.AnecdoteBuilder setAuthor(UserEntity author) {
            Anecdote.this.author = author;
            return this;
        }

        public Anecdote.AnecdoteBuilder setFileId(String fileId) {
            Anecdote.this.fileId = fileId;
            return this;
        }

        public Anecdote.AnecdoteBuilder setFileType(String fileType) {
            Anecdote.this.fileType = fileType;
            return this;
        }

        public Anecdote.AnecdoteBuilder setName(String name) {
            Anecdote.this.name = name;
            return this;
        }

        public Anecdote build() {
            return Anecdote.this;
        }
    }

}
