package com.example.commerce.admin

import com.example.commerce.books.Books
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.sql.JoinType
import org.jetbrains.exposed.sql.batchInsert
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatusCode
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

@RestController
@RequestMapping("/admin-service")
class AdminController(private val adminService: AdminService) {

    //디렉토리 파일 경로
    private val ADMIN_FILE_PATH = "files/main"

    //오늘의 북 데이터 받아서
    @PostMapping
    fun todayDataToMyServer() {
        println("오늘의 북데이터 가져오기")
        adminService.fetchTodayData()
    }

    @PostMapping("/stock")
    fun stockStatusDataToMyServer() {
        println("책 재고량 가져오기")
        adminService.fetchStockStatusData()
    }

    //해당 날짜의 오늘의 북 데이터 가져오기
    @GetMapping
    fun fetchTodayLetter() : ResponseEntity<TodayLetterResponse> {
        val currentDateTime = LocalDateTime.now()
        // 출력 형식을 정의하기 위한 DateTimeFormatter 사용 (선택 사항)
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val formattedDateTime = currentDateTime.format(formatter)

        val todayData = transaction {
            //                        .join(Books, JoinType.INNER,
//                        onColumn = Books.itemId, otherColumn = TodayBook.itemId)
            val todayData =
                TodayBook.select { TodayBook.createdDate eq "2023-10-31" }
                    .mapNotNull { r ->
                        TodayLetterResponse(
                            r[TodayBook.id].value, r[TodayBook.cover],
                            r[TodayBook.title], r[TodayBook.author],
                            r[TodayBook.priceSales], r[TodayBook.todayLetter],
                            r[TodayBook.itemId], r[TodayBook.createdDate]
                        )
                    }
            return@transaction todayData.firstOrNull()
        }
        if (todayData != null) {
            return ResponseEntity.status(HttpStatus.OK).body(todayData)
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build()

    }

    @PostMapping("/files")
    fun imageFilesToMyServer(dataAPI: List<MultipartFile>): ResponseEntity<Any>{

        val dirPath = Paths.get(ADMIN_FILE_PATH)
        if(!Files.exists(dirPath)) {
            //폴더 생성
            Files.createDirectories(dirPath)
        }
        val fileList = mutableListOf<Map<String, String?>>()

        //runBlocking, launch 코루틴 처리
        runBlocking {
            dataAPI.forEach {
                launch {
                    println("filename: ${it.originalFilename}")

                    val uuidFileName =
                        "${ UUID.randomUUID() }" +
                                ".${ it.originalFilename!!.split(".").last() }"

                    val filePath = dirPath.resolve(uuidFileName)

                    it.inputStream.use {
                        Files.copy(it, filePath, StandardCopyOption.REPLACE_EXISTING)
                    }
                    //파일의 메타데이터를 리스트-맵에 임시저장
                    fileList.add(mapOf("uuidFileName" to uuidFileName,
                        "contentType" to it.contentType,
                        "originalFileName" to it.originalFilename))
                }
            }
        }

        val result = transaction {
            MainFiles.batchInsert(fileList){
                this[MainFiles.uuidFileName] = it["uuidFileName"] as String
                this[MainFiles.originalFileName] = it["originalFileName"] as String
                this[MainFiles.contentType] = it["contentType"] as String
            }
            return@transaction null
        }
        return ResponseEntity.status(HttpStatus.OK).build()

    }


}