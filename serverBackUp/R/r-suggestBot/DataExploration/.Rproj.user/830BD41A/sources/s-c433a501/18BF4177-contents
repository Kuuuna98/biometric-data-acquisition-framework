library(DBI)
library(RMySQL)
library(plyr)
library(dplyr)
library(doMC)
registerDoMC(cores=16)
library(foreach)
library(ggplot2)

mytheme <- theme_bw() + 
  theme(panel.border = element_rect(colour="black",size=1)) +
  theme(panel.grid.minor = element_blank()) +
  theme(panel.grid.major = element_line(colour="#9a9a9a",size=.7, linetype="dashed")) + 
  theme(axis.text.x = element_text(size=rel(1.5))) + 
  theme(axis.title.x = element_text(size=rel(1.5))) +
  theme(axis.title.y = element_text(size=rel(1.5))) +
  theme(axis.text.y = element_text(size=rel(1.5))) +
  theme(axis.text = element_text(colour = "black"))

con <- dbConnect(RMySQL::MySQL(), 
                  username = "root",
                  password = "rootpass12#$",
                  host = "127.0.0.1",
                  port = 3306, 
                  dbname = "drm_data")

query_index_3_22 <- list(
E4BVP="SELECT E4BVP.subject_phoneNumber, Guide.productNo, min(E4BVP.timestamp), min(Guide.startTime), max(E4BVP.timestamp), max(Guide.endTime)
FROM E4BVP, Guide
WHERE E4BVP.subject_phoneNumber = Guide.subject_phoneNumber and (Guide.productNo = 3 or Guide.productNo = 22)
and Guide.startTime < E4BVP.timestamp and E4BVP.timestamp < Guide.endTime
GROUP BY E4BVP.subject_phoneNumber, Guide.productNo;",

E4GSR="SELECT E4SkinTemperature.subject_phoneNumber, Guide.productNo, min(E4SkinTemperature.timestamp), min(Guide.startTime), max(E4SkinTemperature.timestamp), max(Guide.endTime)
FROM E4SkinTemperature, Guide
WHERE E4SkinTemperature.subject_phoneNumber = Guide.subject_phoneNumber and (Guide.productNo = 3 or Guide.productNo = 22)
and Guide.startTime < E4SkinTemperature.timestamp and E4SkinTemperature.timestamp < Guide.endTime
GROUP BY E4SkinTemperature.subject_phoneNumber, Guide.productNo;",

E4IBI="SELECT E4IBI.subject_phoneNumber, Guide.productNo, min(E4IBI.timestamp), min(Guide.startTime), max(E4IBI.timestamp), max(Guide.endTime)
FROM E4IBI, Guide
WHERE E4IBI.subject_phoneNumber = Guide.subject_phoneNumber and (Guide.productNo = 3 or Guide.productNo = 22)
and Guide.startTime < E4IBI.timestamp and E4IBI.timestamp < Guide.endTime
GROUP BY E4IBI.subject_phoneNumber, Guide.productNo;",

E4TEMP="SELECT E4SkinTemperature.subject_phoneNumber, Guide.productNo, min(E4SkinTemperature.timestamp), min(Guide.startTime), max(E4SkinTemperature.timestamp), max(Guide.endTime)
FROM E4SkinTemperature, Guide
WHERE E4SkinTemperature.subject_phoneNumber = Guide.subject_phoneNumber and (Guide.productNo = 3 or Guide.productNo = 22)
and Guide.startTime < E4SkinTemperature.timestamp and E4SkinTemperature.timestamp < Guide.endTime
GROUP BY E4SkinTemperature.subject_phoneNumber, Guide.productNo;",

E4ACC="SELECT E4Accelerometer.subject_phoneNumber, Guide.productNo, min(E4Accelerometer.timestamp), min(Guide.startTime), max(E4Accelerometer.timestamp), max(Guide.endTime)
FROM E4Accelerometer, Guide
WHERE E4Accelerometer.subject_phoneNumber = Guide.subject_phoneNumber and (Guide.productNo = 3 or Guide.productNo = 22)
and Guide.startTime < E4Accelerometer.timestamp and E4Accelerometer.timestamp < Guide.endTime
GROUP BY E4Accelerometer.subject_phoneNumber, Guide.productNo;",

SensorTag="SELECT SensorTag.subject_phoneNumber, Guide.productNo, min(SensorTag.timestamp), min(Guide.startTime), max(SensorTag.timestamp), max(Guide.endTime)
FROM SensorTag, Guide
WHERE SensorTag.subject_phoneNumber = Guide.subject_phoneNumber and (Guide.productNo = 3 or Guide.productNo = 22)
and Guide.startTime < SensorTag.timestamp and SensorTag.timestamp < Guide.endTime
GROUP BY SensorTag.subject_phoneNumber, Guide.productNo;",

SmartphoneAcc="SELECT SmartphoneAccelerometer.subject_phoneNumber, Guide.productNo, min(SmartphoneAccelerometer.timestamp), min(Guide.startTime), max(SmartphoneAccelerometer.timestamp), max(Guide.endTime)
FROM SmartphoneAccelerometer, Guide
WHERE SmartphoneAccelerometer.subject_phoneNumber = Guide.subject_phoneNumber and (Guide.productNo = 3 or Guide.productNo = 22)
and Guide.startTime < SmartphoneAccelerometer.timestamp and SmartphoneAccelerometer.timestamp < Guide.endTime
GROUP BY SmartphoneAccelerometer.subject_phoneNumber, Guide.productNo;",

SmartphoneGyro="SELECT SmartphoneGyroscope.subject_phoneNumber, Guide.productNo, min(SmartphoneGyroscope.timestamp), min(Guide.startTime), max(SmartphoneGyroscope.timestamp), max(Guide.endTime)
FROM SmartphoneGyroscope, Guide
WHERE SmartphoneGyroscope.subject_phoneNumber = Guide.subject_phoneNumber and (Guide.productNo = 3 or Guide.productNo = 22)
and Guide.startTime < SmartphoneGyroscope.timestamp and SmartphoneGyroscope.timestamp < Guide.endTime
GROUP BY SmartphoneGyroscope.subject_phoneNumber, Guide.productNo;"
)

options(scipen=999)
# parallel processing  

system.time(resIndex <- foreach (x = query_index_3_22, .final = function(x) setNames(x, names(query_index_3_22))) %do% {
  library(RMySQL)
  res<-dbGetQuery(con, x)
  names(res) <- c("phone","product","sensor_min","audio_min","sensor_max","audio_max")
  res
})

dfSensorIndex <- ldply(resIndex)

# inspect missing sensor type (correct number = 16)
dfSensorTypes <- dfSensorIndex %>% group_by(phone) %>% dplyr::summarise(n=n())
dfSensorTypesWOibi <- dfSensorIndex %>% filter(.id != "E4IBI") %>% group_by(phone) %>% dplyr::summarise(n=n())

library(ggplot2)
ggplot(dfSensorTypes, aes(x=phone,y=n))+ geom_bar(stat="identity",fill="white",colour="black") +
  mytheme + theme(axis.text.x = element_text(angle=90))

ggplot(dfSensorTypesWOibi, aes(x=phone,y=n))+ geom_bar(stat="identity",fill="white",colour="black") +
  mytheme + theme(axis.text.x = element_text(angle=90))
# compare 
#subset(dfSensorIndex, .id == "E4ACC", select=phone) %in% subset(dfSensorIndex, .id == "SensorTag", select=phone)

# extraction correct phone number that contains all users
dfSensorTypesWOibi %>% filter(n==14)

query_data_14="select * from Guide where (subject_phoneNumber = 01020143772 or 
subject_phoneNumber = 01024864821 or
subject_phoneNumber = 01025209853 or
subject_phoneNumber = 01027840617 or
subject_phoneNumber = 01030907426 or
subject_phoneNumber = 01037711063 or
subject_phoneNumber = 01043082008 or
subject_phoneNumber = 01049151225 or
subject_phoneNumber = 01050140044 or
subject_phoneNumber = 01052935728 or
subject_phoneNumber = 01058937288 or
subject_phoneNumber = 01073451686 or
subject_phoneNumber = 01079071567 or
subject_phoneNumber = 01088084121 ) and (productNo = 3 or productNo =22);"

query_data_14_all = "select * from Guide;"

result_allsensor_index <- dbGetQuery(con,query_data_14)



sensor_table <- RMySQL::dbListTables(con)[c(1,2,3,5,9,10,11)]
sensor_table


query_senosr_extraction_list <- list()
for (table_name in sensor_table){
  tmp <- list(table_name = apply(result_allsensor_index,1,function(x){
    query_E4GSR = sprintf("select * from %s where %s <= %s.timestamp and %s >= %s.timestamp and %s.subject_phoneNumber = %s;",
                          table_name,x[4],table_name,x[3],table_name,table_name,x[5])
  }))
  query_senosr_extraction_list <- append(query_senosr_extraction_list, tmp)
}
names(query_senosr_extraction_list) <- sensor_table

system.time(euc_e4acc <- ldply(query_senosr_extraction_list[["E4Accelerometer"]],function(x){
  #library(RMySQL)
  tmp=dbGetQuery(con,x)
  eu_tmp <- feature_extraction_E4ACC(tmp[,c("x","y","z")])
  res<-data.frame(E4Acc_Min=summary(eu_tmp)["Min."],
                  E4Acc_Q1=summary(eu_tmp)["1st Qu."],
                  E4Acc_Median=summary(eu_tmp)["Median"],
                  E4Acc_Mean=summary(eu_tmp)["Mean"],
                  E4Acc_Q3=summary(eu_tmp)["3rd Qu."],
                  E4Acc_Max=summary(eu_tmp)["Max."])
  res
}, .parallel = FALSE))



e4bvp <- ldply(query_senosr_extraction_list[["E4BVP"]],function(x){
  tmp=dbGetQuery(con,x)
  res<-data.frame(E4bvp_Min=summary(tmp$value)["Min."],
                  E4bvp_Q1=summary(tmp$value)["1st Qu."],
                  E4bvp_Median=summary(tmp$value)["Median"],
                  E4bvp_Mean=summary(tmp$value)["Mean"],
                  E4bvp_Q3=summary(tmp$value)["3rd Qu."],
                  E4bvp_Max=summary(tmp$value)["Max."])
  res
})

e4gsr <- ldply(query_senosr_extraction_list[["E4GSR"]],function(x){
  tmp=dbGetQuery(con,x)
  res<-data.frame(E4gsr_Min=summary(tmp$value)["Min."],
                  E4gsr_Q1=summary(tmp$value)["1st Qu."],
                  E4gsr_Median=summary(tmp$value)["Median"],
                  E4gsr_Mean=summary(tmp$value)["Mean"],
                  E4gsr_Q3=summary(tmp$value)["3rd Qu."],
                  E4gsr_Max=summary(tmp$value)["Max."])
  res
})

e4temp <- ldply(query_senosr_extraction_list[["E4SkinTemperature"]],function(x){
  tmp=dbGetQuery(con,x)
  res<-data.frame(E4temp_Min=summary(tmp$value)["Min."],
                  E4temp_Q1=summary(tmp$value)["1st Qu."],
                  E4temp_Median=summary(tmp$value)["Median"],
                  E4temp_Mean=summary(tmp$value)["Mean"],
                  E4temp_Q3=summary(tmp$value)["3rd Qu."],
                  E4temp_Max=summary(tmp$value)["Max."])
  res
})

sensortag <- ldply(query_senosr_extraction_list[["SensorTag"]],function(x){
  tmp=dbGetQuery(con,x)
  eu_tmp <- feature_extraction_E4ACC(tmp[,c("accelerometer_x","accelerometer_y","accelerometer_z")])
  res<-data.frame(sensortag_Min=summary(eu_tmp)["Min."],
                  sensortag_Q1=summary(eu_tmp)["1st Qu."],
                  sensortag_Median=summary(eu_tmp)["Median"],
                  sensortag_Mean=summary(eu_tmp)["Mean"],
                  sensortag_Q3=summary(eu_tmp)["3rd Qu."],
                  sensortag_Max=summary(eu_tmp)["Max."])
  res
})

# SmartphoneAccelerometer
euc_phone_acc <- ldply(query_senosr_extraction_list[["SmartphoneAccelerometer"]], function(x){
  tmp=dbGetQuery(con,x)
  eu_tmp <- feature_extraction_E4ACC(tmp[,c("x","y","z")])
  res<-data.frame(phone_acc_Min=summary(eu_tmp)["Min."],
                  phone_acc_Q1=summary(eu_tmp)["1st Qu."],
                  phone_acc_Median=summary(eu_tmp)["Median"],
                  phone_acc_Mean=summary(eu_tmp)["Mean"],
                  phone_acc_Q3=summary(eu_tmp)["3rd Qu."],
                  phone_acc_Max=summary(eu_tmp)["Max."])
  res
})


df_feature_extraction <- cbind.data.frame(result_allsensor_index, euc_e4acc, e4bvp, e4gsr, e4temp, sensortag, euc_phone_acc)
write.csv(df_feature_extraction,file="./df_feature_extraction_subject_phone_number.csv")



b<- feature_extraction_E4ACC(a[,c("x","y","z")])
# Feature Extraction ------------------------------------------------------
# E4
feature_extraction_E4BVP <- function(x){
  res<-data.frame(Min=summary(x)["Min."],
             Q1=summary(x)["1st Qu."],
             Median=summary(x)["Median"],
             Mean=summary(x)["Mean"],
             Q3=summary(x)["3rd Qu."],
             Max=summary(x)["Max."])
  res
} 

euc.dist <- function(x1, x2) sqrt(sum((x1 - x2) ^ 2))
feature_extraction_E4ACC <- function(x){
  distanceEu <- apply(x,1,function(x){
    res<-euc.dist( as.numeric(x[1]), euc.dist( as.numeric(x[2]), as.numeric(x[3])))
    res
  })  
}



feature_extraction_E4TEMP <- function(x){
  
} 
feature_extraction_E4GSR <- function(x){
  
} 

# smartphone
feature_extraction_E4ACC <- function(x){
  
}
# sensorTag
feature_extraction_sensortag <- function(x){
  
}



# find raw data
query_E4GSR = sprintf("select * from E4BVP where %s <= E4BVP.timestamp and %s >= E4BVP.timestamp;",1529390324322,1529390359620)
query_E4BVP = sprintf("select * from E4Accelerometer where %s <= E4Accelerometer.timestamp and %s >= E4Accelerometer.timestamp;",1529390324334,1529390359617)
query_E4TEMP = sprintf("select * from E4Accelerometer where %s <= E4Accelerometer.timestamp and %s >= E4Accelerometer.timestamp;",1529390324334,1529390359617)
query_E4ACC = sprintf("select * from E4Accelerometer where %s <= E4Accelerometer.timestamp and %s >= E4Accelerometer.timestamp;",1529390324334,1529390359617)

query_SPACC = sprintf("select * from E4Accelerometer where %s <= E4Accelerometer.timestamp and %s >= E4Accelerometer.timestamp;",1529390324334,1529390359617)
query_SPGYRO = sprintf("select * from E4Accelerometer where %s <= E4Accelerometer.timestamp and %s >= E4Accelerometer.timestamp;",1529390324334,1529390359617)
query_SENSORTAGACC = sprintf("select * from E4Accelerometer where %s <= E4Accelerometer.timestamp and %s >= E4Accelerometer.timestamp;",1529390324334,1529390359617)

result.E4.BVP.3 <- dbGetQuery(con,query_test)
result.E4.ACC.3 <- dbGetQuery(con,query_test2)
result.E4.ACC.3 <- dbGetQuery(con,query_test2)


op <- options(digits.secs=3)
options(op) #reset options

result.E4.1 <- result.E4.1 %>% mutate(date = as.character(as.POSIXct(as.numeric(timestamp)/1000,
                                        origin="1970-01-01")))





# frequency analysis
b1.x.fft <- fft(result.E4.1$value)
# Ignore the 2nd half, which are complex conjugates of the 1st half, 
# and calculate the Mod (magnitude of each complex number)
amplitude <- Mod(b1.x.fft[1:(length(b1.x.fft)/2)])
# Calculate the frequencies
frequency <- seq(0, 2259, length.out=length(b1.x.fft)/2)
# Plot!
plot(amplitude ~ frequency[1:2259], t="l")




library(foreach) 
system.time(l2 <- foreach(i=1:4, .combine = 'c') %do% 
              rnorm(2500000))

system.time(l3<-ldply(as.list(rnorm(2500000)), .parallel = TRUE))
                                                                                 
                                                                                 
                                                                                 
                                                                                 
                                                                                 
