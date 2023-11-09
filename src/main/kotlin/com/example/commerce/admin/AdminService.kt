package com.example.commerce.admin

import com.example.commerce.auth.Profiles
import com.example.commerce.books.AlamBooks
import com.example.commerce.books.Books
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.LoggerFactory
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.web.multipart.MultipartFile
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*


@EnableScheduling
@Component
class AdminService(
    private val adminClient: AdminClient,
    private val adminApiClient: AdminApiClient,
    @Autowired
    @Qualifier("rabbitTemplate2") private val rabbitTemplate2: RabbitTemplate) {

    private val mapper = jacksonObjectMapper()
    //에러 로그 확인을 위해
    private val logger = LoggerFactory.getLogger(this.javaClass.name)

    //디렉토리 파일 경로
    private val ADMIN_FILE_PATH = "files/main"

//    @Scheduled(cron = "0 0 0 * * *")
    @Scheduled(cron = "0 45 9 * * *")
    fun fetchTodayData() {
        val currentDateTime = LocalDateTime.now()
        // 출력 형식을 정의하기 위한 DateTimeFormatter 사용 (선택 사항)
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val formattedDateTime = currentDateTime.format(formatter)

        try {
            println("데이터 받기 시작해요!!")
            val dataAPI: TodayDataResponse = adminClient.getTodayBook("2023-10-31")

            val existingToday = TodayBook.select { TodayBook.itemId eq dataAPI.itemId }.singleOrNull()

            if (existingToday == null) {
                logger.info("오늘의 책 DB 입력 시작")
                transaction {
                    TodayBook.insert {
                        it[cover] = dataAPI.cover
                        it[title] = dataAPI.title
                        it[author] = dataAPI.author
                        it[priceSales] = dataAPI.priceSales
                        it[todayLetter] = dataAPI.todayLetter
                        it[itemId] = dataAPI.itemId
                        it[createdDate] = formattedDateTime
                    }
                }
                logger.info("오늘의 책 DB 입력 성공")
            }else {
                logger.info("이미 오늘의 책이 존재합니다.")
            }
        }catch (e: Exception){
            logger.error("오류 발생: ${e.message}")
        }
    }

    fun uploadImageFileData() : ResponseEntity<Any> {
        try {
            val dataAPI: List<MainFileResponse> = adminClient.downloadFile()
            for (data in dataAPI){
                val result = transaction {
                    MainFiles.insert {
                        it[image] = data.image
                        it[link] = data.link
                    }
                }
            }
            return ResponseEntity.status(HttpStatus.OK).build()
        }catch (e: Exception) {
            logger.error(e.message)
            return ResponseEntity.status(HttpStatus.CONFLICT).build()
        }
    }

    //메인 베너 이미지 파일 배열
//    fun uploadImageFileData() : ResponseEntity<Any>{
//        try{
//            val dataAPI: List<MultipartFile> = adminClient.downloadFile()
//            val dirPath = Paths.get(ADMIN_FILE_PATH)
//            if(!Files.exists(dirPath)) {
//                //폴더 생성
//                Files.createDirectories(dirPath)
//            }
//            val fileList = mutableListOf<Map<String, String?>>()
//
//            //runBlocking, launch 코루틴 처리
//            runBlocking {
//                dataAPI.forEach {
//                    launch {
//                        println("filename: ${it.originalFilename}")
//
//                        val uuidFileName =
//                            "${ UUID.randomUUID() }" +
//                                    ".${ it.originalFilename!!.split(".").last() }"
//
//                        val filePath = dirPath.resolve(uuidFileName)
//
//                        it.inputStream.use {
//                            Files.copy(it, filePath, StandardCopyOption.REPLACE_EXISTING)
//                        }
//                        //파일의 메타데이터를 리스트-맵에 임시저장
//                        fileList.add(mapOf("uuidFileName" to uuidFileName,
//                            "contentType" to it.contentType,
//                            "originalFileName" to it.originalFilename))
//                    }
//                }
//            }
//
//            val result = transaction {
//                val result = MainFiles.batchInsert(fileList){
//                    this[MainFiles.uuidFileName] = it["uuidFileName"] as String
//                    this[MainFiles.originalFileName] = it["originalFileName"] as String
//                    this[MainFiles.contentType] = it["contentType"] as String
//                }
//                return@transaction result
//            }
//            return ResponseEntity.status(HttpStatus.OK).body(result)
//        }catch (e: Exception) {
//            logger.error(e.message)
//            return ResponseEntity.status(HttpStatus.CONFLICT).build()
//        }
//    }

    //조회수 레빗 mq
    fun sendHits(hits: HitsDataResponse){
        println("이제 진짜 레빗으로 가요")

        rabbitTemplate2.convertAndSend("hits-queue", mapper.writeValueAsString(hits))
    }

    fun sendRabbitData(itemId:Int, profileId:Long?){

        val currentDateTime = LocalDateTime.now()
        // 출력 형식을 정의하기 위한 DateTimeFormatter 사용 (선택 사항)
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
        val formattedDateTime = currentDateTime.format(formatter)


        println("레빗 객체 생성 시작")
        val (result, newHits) = transaction {
            val result = HitsTable.insert {
                it[this.itemId] = itemId
                it[this.hitsCount] = 1
                if (profileId != null) {
                    it[this.profileId] = profileId
                }
                it[this.createdDate] = formattedDateTime
            }.resultedValues ?: return@transaction Pair(false, null)
            val record = result.first()
            if (profileId != null) {
                val birth = (Profiles innerJoin HitsTable)
                    .select { HitsTable.profileId eq profileId }.firstNotNullOf {
                        it[Profiles.birth]
                    }.toString()

                val hits = (Profiles innerJoin HitsTable)
                    .select { HitsTable.profileId eq profileId }
                    .mapNotNull {
                            r->
                        var newBirth = 0
                        var newGender = 0
                        if(birth != null) {
                            newBirth = birth.substring(0, 2).toInt()
                            newGender = birth.substring(6).toInt()
                        }
                        HitsDataResponse(
                            record[HitsTable.itemId],
                            r[Profiles.nickname],
                            birth = newBirth,
                            r[Profiles.bookmark],
                            record[HitsTable.hitsCount],
                            record[HitsTable.createdDate],
                            gender = newGender,
                        )
                    }
                return@transaction Pair(true, hits.first())
            } else {
                return@transaction Pair(true, HitsDataResponse(
                    record[HitsTable.itemId],
                    null,
                    null,
                    null,
                    record[HitsTable.hitsCount],
                    record[HitsTable.createdDate],
                    null

                ))
            }

        }
        //조회수 row 객체 보내기
        if(result){
            println("신간데이터 레빗으로 보내요")
            val hitsDataResponse = newHits as? HitsDataResponse
            if(hitsDataResponse != null){
                println(hitsDataResponse)
                sendHits(hitsDataResponse)
            }

        }
    }

    @Scheduled(cron = "0 45 9 * * *")
    fun fetchStockStatusData() {
        val currentDateTime = LocalDateTime.now()
        // 출력 형식을 정의하기 위한 DateTimeFormatter 사용 (선택 사항)
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val formattedDateTime = currentDateTime.format(formatter)
        println(formattedDateTime)

        try {
            logger.info("책 재고 DB 업데이트 시작")
            val dataAPI: List<StockStatusResponse> = adminApiClient.fetchStockStatus(formattedDateTime)
            transaction {
                for(data in dataAPI){
                    val itemId = data.itemId.toInt()
                    Books.update({ Books.itemId eq itemId }) {
                        it[stockStatus] = data.stockStatus
                    }
                    if(data.stockStatus.toInt() > 0) {
                        AlamBooks.update({AlamBooks.bookItemId eq itemId}){
                            // 해당 아이템에 대한 alamDisplay의 값을 가져옵니다
                            val alamDisplayValue = AlamBooks.select { AlamBooks.bookItemId eq itemId }
                                .singleOrNull()
                                ?.get(AlamBooks.alamDisplay)

                            // alamDisplay의 값을 확인하고 그에 따라 alam을 업데이트합니다
                            alamDisplayValue?.let { displayValue ->
                                it[alam] = displayValue // alamDisplay 값에 따라 alam을 업데이트합니다
                            }
                        }
                    }
                }
            }
            logger.info("책 재고 DB 업데이트 성공")
        }catch (e: Exception){
            logger.error("오류 발생: ${e.message}")
        }
    }

}