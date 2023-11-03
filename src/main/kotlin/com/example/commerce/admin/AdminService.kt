package com.example.commerce.admin

import com.example.commerce.auth.Profiles
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.LoggerFactory
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.web.multipart.MultipartFile
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


@EnableScheduling
@Component
class AdminService(
    private val adminClient: AdminClient,
    @Qualifier("rabbitTemplate1") private val rabbitTemplate: RabbitTemplate,
    private val adminController: AdminController) {

    private val mapper = jacksonObjectMapper()
    //에러 로그 확인을 위해
    private val logger = LoggerFactory.getLogger(this.javaClass.name)

//    @Scheduled(cron = "0 0 0 * * *")
    @Scheduled(cron = "0 45 9 * * *")
    fun fetchTodayData() {
        println("오늘의 북데이터 가져오기")
        val currentDateTime = LocalDateTime.now()
        // 출력 형식을 정의하기 위한 DateTimeFormatter 사용 (선택 사항)
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val formattedDateTime = currentDateTime.format(formatter)

        try {
            println("데이터 받기 시작해요!!")
            val dataAPI: TodayDataResponse = adminClient.getTodayBook("2023-10-31")

            adminController.todayDataToMyServer(dataAPI)
        }catch (e: Exception){
        logger.error(e.message)
        }
    }
    fun fetchImageFileData(){
        println("메인베너 이미지 배열 가져오기")
        try{
            val dataAPI: List<MultipartFile> = adminClient.downloadFile()
        }catch (e: Exception){
            logger.error(e.message)
        }
    }

    //조회수 레빗 mq
    fun sendHits(hits: HitsDataResponse){
        println("이제 진짜 레빗으로 가요")

        rabbitTemplate.convertAndSend("hits-queue", mapper.writeValueAsString(hits))
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
                            newBirth = birth.substring(1, 3).toInt()
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

}