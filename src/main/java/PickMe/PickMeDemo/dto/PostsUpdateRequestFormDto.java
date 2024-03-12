package PickMe.PickMeDemo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder

public class PostsUpdateRequestFormDto {

    private String title;
    private List<String> postType; // Assuming postType is an array of strings
    private Integer recruitmentCount;
    private LocalDate endDate;
    private String content;
    private List<MultipartFile> newPromoteImageUrl; // 새로 추가할 이미지 파일 자체를 리스트로 받음
    private List<String> promoteImageUrl; // 기존의 이미지에서 변경된 사진 uuid값 리스트
    private List<MultipartFile> newFileUrl; // 새로 추가할 첨부 파일 자체를 리스트로 받음
    private List<FileUrlNameMapperDto> fileUrl; // 기존의 첨부파일에서 변경된 첨부파일(uuid, 원본파일 이름) 리스트

}
