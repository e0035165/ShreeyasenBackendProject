package org.entity;


import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "Documents")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@Builder
public class Documents {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "title")
    private String title;

    @Column(name = "fileType")
    private String fileType;

    @Column(name="file-path")
    private String filePath;

    @Column(name="relative_file_name", unique = true)
    private String relative_file_name;

    @Lob
    private byte[] data;

}
