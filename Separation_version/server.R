library(shiny)
library(ggplot2)
library(ggvis)
library(reshape)
library(gridExtra)
# library(quantreg)
# library(plyr)

#for Large Data
options("scipen"=100)
options(digits = 13)
# options(shiny.maxRequestSize=30*1024^2)
source("functions.R")

#call functions of functions.R by receiving user input
function(input, output){
  
  #if you select file (default is all file), lungdata and e4Data is automatically change
  lungdata <- reactive({
    #select each user's file in phone number format
    #input$selectFile_check is selected file name(phone number format)
    inFile<-input$selectFile_check
    
    if(is.null(inFile)) #if file is null, return null
      return(NULL)
    
    lungdata<-NULL
    #for selected file list(phone number format), 
    #get paths of files(date format) in each file(phone number format)
    #read csv file each path
    #bind to lungdata
    for(var in input$selectFile_check) {
      path<-paste0(as.character(entireData[which(entireData$udid==var),3]),"/Phone.csv")
      
      for (subpath in path) {
        lungdata <- rbind(lungdata,read.csv(subpath))
      }
      
    }
    #organize data set
    lungdata<-lungdata[,c(2,1,3:11)]
    colnames(lungdata)<-c("productNo","Num","sensingTime","acc_X","acc_Y","acc_Z","gyro_X","gyro_Y","gyro_Z","longitude","latitude")
    #order by sensing time
    lungdata <- lungdata[c(order(lungdata$sensingTime)),]
    
  })
  
  e4Data <- reactive({
    inFile<-input$selectFile_check
    if(is.null(inFile))
      return(NULL)
    
    e4Data<-NULL
    #for selected file list(phone number format), 
    #get paths of files(date format) in each file(phone number format)
    #read csv file each path
    #bind to e4Data
    for(var in input$selectFile_check) {
      path<-paste0(as.character(entireData[which(entireData$udid==var),3]),"/E4.csv")
      for (subpath in path) {
        e4Data <- rbind(e4Data,read.csv(subpath))
      }
      
    }
    
    #organize data set
    e4Data<-e4Data[,c(2,1,3:10)]
    
    colnames(e4Data) <- c("lungdata.productNo", "lungdata.Num","lungdata.sensingTime","lungdata.E4_temp", "lungdata.E4_bvp", "lungdata.E4_ibi", "lungdata.E4_accX","lungdata.E4_accY","lungdata.E4_accZ","lungdata.E4_gsr")
    #order by sensing time
    e4Data <- e4Data[c(order(e4Data$lungdata.sensingTime)),]
    
  })
  #for default mode
  output$graph <- renderPlot({
    inputData <- NULL
    inputLo <-NULL
    inputE4Data <- NULL
    
    #input time (start time ~ end time)
    startTime <- paste0(input$selectHour_s,":",input$selectMin_s,":",input$selectSec_s)
    endTime <- paste0(input$selectHour_e,":",input$selectMin_e,":",input$selectSec_e)
    i= c(strptime(paste(input$selectDate[1],startTime),"%Y-%m-%d %H:%M:%S",tz="GMT-9"),strptime(paste(input$selectDate[2],endTime),"%Y-%m-%d %H:%M:%S",tz="GMT-9"))
    
    #call reactive module for data table
    e4Data<-e4Data()
    timeDate <- lungdata()
    timeE4Date<- e4Data()
    
    #convert lungdata&e4Data sensingTime millisecond to second
    #for compare
    timeDate$sensingTime <- as.POSIXct(lungdata()$sensingTime/1000,origin="1970-01-01",tz="GMT-9")
    timeE4Date$lungdata.sensingTime <- as.POSIXct(e4Data$lungdata.sensingTime/1000,origin="1970-01-01",tz="GMT-9")
    
    #check data in selected time is null
    if(length(which(i[1]<=timeDate$sensingTime  & timeDate$sensingTime <= i[2]))!=0){
      inputData <- lungdata()[which(i[1]<=timeDate$sensingTime  & timeDate$sensingTime <= i[2]),]
      inputLo <- inputData[which(input$selectLo[1] <= inputData$longitude & inputData$longitude <=input$selectLo[2] & input$selectLa[1]<= inputData$latitude & inputData$latitude <= input$selectLa[2]),]
    }else {
      inputData<-NULL
      inputLo<-NULL
    }
    if(length(which(i[1]<=timeE4Date$lungdata.sensingTime  & timeE4Date$lungdata.sensingTime <= i[2]))!=0){
      inputE4Data <- e4Data[which(i[1]<= timeE4Date$lungdata.sensingTime & timeE4Date$lungdata.sensingTime <= i[2]),]
    }else inputE4Data<-NULL
   
    #if data table is null,occurs data type error in functions.R
    #make empty data table
    
    if(is.null(inputE4Data)){
      inputE4Data <- data.frame(NA,NA,NA,NA,NA,NA,NA,NA,NA,NA)
      colnames(inputE4Data)<-c("lungdata.productNo", "lungdata.Num","lungdata.sensingTime","lungdata.E4_temp", "lungdata.E4_bvp", "lungdata.E4_ibi", "lungdata.E4_accX","lungdata.E4_accY","lungdata.E4_accZ","lungdata.E4_gsr")
    }
    if(is.null(inputData)){
      inputData <- data.frame(NA,NA,NA,NA,NA,NA,NA,NA,NA,NA,NA)
      colnames(inputData)<-c("productNo","Num","sensingTime","acc_X","acc_Y","acc_Z","gyro_X","gyro_Y","gyro_Z","longitude","latitude")
    }
    if(is.null(inputLo)){
      inputLo <- data.frame(NA,NA,NA,NA,NA,NA,NA,NA,NA,NA,NA)
      colnames(inputLo)<-c("productNo","Num","sensingTime","acc_X","acc_Y","acc_Z","gyro_X","gyro_Y","gyro_Z","longitude","latitude")
    }
    #call function in functions.R
    if(nrow(inputData)!=0 || nrow(inputE4Data)!=0){
      switch(input$selectGraph,
             "Location"=getLocation(inputLo),
             "Accelerometer"=getAcceleromter(inputData,inputE4Data,input$selectUser),
             "Gyroscope"=getGyro(inputData,input$selectUser),
             "Temperature"= getTemp(inputE4Data,input$selectUser),
             "Inter Beat Interval"=getIBI(inputE4Data,input$selectUser),
             "Blood Volume Pulse"=getBVP(inputE4Data,input$selectUser),
             "GSR"= getGSR(inputE4Data,input$selectUser)
      )
      
    }
    else{
      #if data table is empty, return blank plot
      ggplot()+theme_bw()
    }
    
  }, height = 740)
  
  #in Data set mode, selected phone data
  output$dataTable <- renderDataTable(expr = {
    #get data of selected user
    inputData <- lungdata()[which(lungdata()[,1]==input$selectUser2),]
    
    #input time
    startTime <- paste0(input$selectHour_s2,":",input$selectMin_s2,":",input$selectSec_s2)
    endTime <- paste0(input$selectHour_e2,":",input$selectMin_e2,":",input$selectSec_e2)
    i= c(strptime(paste(input$selectDate2[1],startTime),"%Y-%m-%d %H:%M:%S",tz="GMT-9"),strptime(paste(input$selectDate2[2],endTime),"%Y-%m-%d %H:%M:%S",tz="GMT-9"))
    
    #convert lungdata sensingTime millisecond to second
    #for compare
    timeDate <-inputData
    timeDate$sensingTime <- as.POSIXct(timeDate$sensingTime/1000,origin="1970-01-01",tz="GMT-9")
    
    #check data in selected time is null
    if(length(which(i[1]<=timeDate$sensingTime  & timeDate$sensingTime <= i[2]))!=0){
      inputData <- inputData[which(i[1]<=timeDate$sensingTime  & timeDate$sensingTime <= i[2]),]
      inputData <- inputData[which(input$selectLo2[1] <= inputData$longitude & inputData$longitude <=input$selectLo2[2] & input$selectLa2[1]<= inputData$latitude & inputData$latitude <= input$selectLa2[2]),]
    }else{
      #if data table is null, return null
      return(NULL)
    }
    
    #show selected column
    datas <- c(3)#time
    if(length(input$selectData2[which(input$selectData2=="Accelerometer")])!=0) datas = c(datas, 4:6)
    if(length(input$selectData2[which(input$selectData2=="Gyroscope")])!=0) datas = c(datas, 7:9)
    if(length(input$selectData2[which(input$selectData2=="Location")])!=0) datas = c(datas, 10:11)
    #bind realTime(convert to second)
    realTime<-as.POSIXct(inputData[,3]/1000,origin="1970-01-01",tz="GMT-9")
    dataTable<-cbind(realTime,inputData[,datas])
    dataTable
  })
  
  output$e4dataTable <- renderDataTable(expr = {
    #get data of selected user
    inputE4Data <- e4Data()[which(e4Data()[,1]==input$selectUser2_1),]
    
    #input time
    startTime <- paste0(input$selectHour_s2_1,":",input$selectMin_s2_1,":",input$selectSec_s2_1)
    endTime <- paste0(input$selectHour_e2_1,":",input$selectMin_e2_1,":",input$selectSec_e2_1)
    i= c(strptime(paste(input$selectDate2_1[1],startTime),"%Y-%m-%d %H:%M:%S",tz="GMT-9"),strptime(paste(input$selectDate2_1[2],endTime),"%Y-%m-%d %H:%M:%S",tz="GMT-9"))
    
    #convert e4Data sensingTime millisecond to second
    #for compare
    timeE4Date<-inputE4Data
    timeE4Date$lungdata.sensingTime <- as.POSIXct(timeE4Date$lungdata.sensingTime/1000,origin="1970-01-01",tz="GMT-9")
    
    #check data in selected time is null
    if(length(which(i[1]<=timeE4Date$lungdata.sensingTime  & timeE4Date$lungdata.sensingTime <= i[2]))!=0){
      inputE4Data <- inputE4Data[which(i[1]<=timeE4Date$lungdata.sensingTime  & timeE4Date$lungdata.sensingTime <= i[2]),]
    }
    else{
      #if data table is null, return null
      return(NULL)
    }
    #show selected column
    E4datas <- c(3)
    if(length(input$selectData2_1[which(input$selectData2_1=="Accelerometer")])!=0)  E4datas<-c(E4datas,7:9)
    if(length(input$selectData2_1[which(input$selectData2_1=="Temperature")])!=0) E4datas = c(E4datas, 4)
    if(length(input$selectData2_1[which(input$selectData2_1=="Blood Volume Pulse")])!=0) E4datas = c(E4datas,5)
    if(length(input$selectData2_1[which(input$selectData2_1=="Inter Beat Interval")])!=0) E4datas = c(E4datas, 6)
    if(length(input$selectData2_1[which(input$selectData2_1=="GSR")])!=0) E4datas = c(E4datas, 10)
    colnames(inputE4Data)<-c("ProductNo","id","E4sensingTime","E4_temp", "E4_bvp", "E4_ibi", "E4_accX","E4_accY","E4_accZ","E4_gsr")
    #bind realTime(convert to second)
     realTime<-as.POSIXct(inputE4Data[,3]/1000,origin="1970-01-01",tz="GMT-9")
    e4dataTable<-cbind(realTime,inputE4Data[,E4datas])
    return(e4dataTable)
  })
  
  #in customize mode > multi mode
  output$customize_graph <- renderPlot({
    inputData <- NULL
    inputE4Data<-NULL
    
    #input time
    startTime <- paste0(input$selectHour_s3,":",input$selectMin_s3,":",input$selectSec_s3)
    endTime <- paste0(input$selectHour_e3,":",input$selectMin_e3,":",input$selectSec_e3)
    i= c(strptime(paste(input$selectDate3[1],startTime),"%Y-%m-%d %H:%M:%S",tz="GMT-9"),strptime(paste(input$selectDate3[2],endTime),"%Y-%m-%d %H:%M:%S",tz="GMT-9"))
    
    #convert lungdata&e4Data sensingTime millisecond to second
    #for compare
    timeDate <- lungdata()
    timeDate$sensingTime <- as.POSIXct(lungdata()$sensingTime/1000,origin="1970-01-01",tz="GMT-9")
    timeE4Date <- e4Data()
    timeE4Date$lungdata.sensingTime <- as.POSIXct(e4Data()$lungdata.sensingTime/1000,origin="1970-01-01",tz="GMT-9")
    
    
    #check data in selected time is null
    if(length(which(i[1]<=timeDate$sensingTime  & timeDate$sensingTime <= i[2]))!=0) inputData <- lungdata()[which(i[1]<=timeDate$sensingTime  & timeDate$sensingTime <= i[2]),]
    else inputData<-NULL
    if(length(which(i[1]<=timeE4Date$lungdata.sensingTime  & timeE4Date$lungdata.sensingTime <= i[2]))!=0) inputE4Data <- e4Data()[which(i[1]<=timeE4Date$lungdata.sensingTime  & timeE4Date$lungdata.sensingTime <= i[2]),]
    else inputE4Data<-NULL
   
    #if data table is null,occurs data type error in functions.R
    #make empty data table
    if(is.null(inputE4Data)){
      inputE4Data <- data.frame(NA,NA,NA,NA,NA,NA,NA,NA,NA,NA)
      colnames(inputE4Data)<-c("lungdata.productNo", "lungdata.Num","lungdata.sensingTime","lungdata.E4_temp", "lungdata.E4_bvp", "lungdata.E4_ibi", "lungdata.E4_accX","lungdata.E4_accY","lungdata.E4_accZ","lungdata.E4_gsr")
    }
    if(is.null(inputData)){
      inputData <- data.frame(NA,NA,NA,NA,NA,NA,NA,NA,NA,NA,NA)
      colnames(inputData)<-c("productNo","Num","sensingTime","acc_X","acc_Y","acc_Z","gyro_X","gyro_Y","gyro_Z","longitude","latitude")
    }
    
    #in multi mode, productNo(column number) must be selected
    selectList<-c(1)
    e4selectList<-c(1)
    
    #selected x-axis & y-axis input
    inputXAxis <-input$X_Axis
    inputYAxis<-input$Y_Axis
    
    switch (input$X_Axis,
           
            #there are two time (e4 time, phone time)
            "Time"={
              selectList<- c(selectList,3)
              e4selectList<-c(e4selectList,3)
              inputXAxis<-paste0(inputXAxis," (ms)") #adding units
            },
            "Accelerometer-X"={
              selectList<- c(selectList,4)
              inputXAxis<-paste0(inputXAxis," (m/s2)")
            },
            "Accelerometer-Y"={
              selectList<- c(selectList,5)
              inputXAxis<-paste0(inputXAxis," (m/s2)")
            },
            "Accelerometer-Z"={
              selectList<- c(selectList,6)
              inputXAxis<-paste0(inputXAxis," (m/s2)")
            },
            "Gyroscope-X"={
              selectList<- c(selectList,7)
              inputXAxis<-paste0(inputXAxis," (degree/sec)")
            },
            "Gyroscope-Y"={
              selectList<- c(selectList,8)
              inputXAxis<-paste0(inputXAxis," (degree/sec)")
            },
            "Gyroscope-Z"={
              selectList<- c(selectList,9)
              inputXAxis<-paste0(inputXAxis," (degree/sec)")
            },
            "E4Accelerometer-X"={
              e4selectList<- c(e4selectList,7)
              inputXAxis<-paste0(inputXAxis," (m/s2)")
            },
            "E4Accelerometer-Y"={
              e4selectList<- c(e4selectList,8)
              inputXAxis<-paste0(inputXAxis," (m/s2)")
            },
            "E4Accelerometer-Z"={
              e4selectList<- c(e4selectList,9)
              inputXAxis<-paste0(inputXAxis," (m/s2)")
            },
            "Latitude"={
              selectList<- c(selectList,11)
              inputXAxis<-paste0(inputXAxis," (°N)")
            },
            "Longitude"={
              selectList<- c(selectList,10)
              inputXAxis<-paste0(inputXAxis," (°E)")
            },
            "Temperature"={
              e4selectList<- c(e4selectList,4)
              inputXAxis<-paste0(inputXAxis," (°C)")
            },
            "IBI"={
              e4selectList<- c(e4selectList,6)
              inputXAxis<-paste0(inputXAxis," (s)")
            },
            "BVP"=e4selectList<- c(e4selectList,5),
            "GSR"={
              e4selectList<- c(e4selectList,10)
              inputXAxis<-paste0(inputXAxis," (μS)")
            }
    )
    switch (input$Y_Axis,
            "Time"={
              selectList<- c(selectList,3)
              e4selectList<-c(e4selectList,3)
              inputYAxis<-paste0(inputYAxis," (ms)")
            },
            "Accelerometer-X"={
              selectList<- c(selectList,4)
              inputYAxis<-paste0(inputYAxis," (m/s2)")
            },
            "Accelerometer-Y"={
              selectList<- c(selectList,5)
              inputYAxis<-paste0(inputYAxis," (m/s2)")
            },
            "Accelerometer-Z"={
              selectList<- c(selectList,6)
              inputYAxis<-paste0(inputYAxis," (m/s2)")
            },
            "Gyroscope-X"={
              selectList<- c(selectList,7)
              inputYAxis<-paste0(inputYAxis," (degree/sec)")
            },
            "Gyroscope-Y"={
              selectList<- c(selectList,8)
              inputYAxis<-paste0(inputYAxis," (degree/sec)")
            },
            "Gyroscope-Z"={
              selectList<- c(selectList,9)
              inputYAxis<-paste0(inputYAxis," (degree/sec)")
            },
            "E4Accelerometer-X"={
              e4selectList<- c(e4selectList,7)
              inputYAxis<-paste0(inputYAxis," (m/s2)")
            },
            "E4Accelerometer-Y"={
              e4selectList<- c(e4selectList,8)
              inputYAxis<-paste0(inputYAxis," (m/s2)")
            },
            "E4Accelerometer-Z"={
              e4selectList<- c(e4selectList,9)
              inputYAxis<-paste0(inputYAxis," (m/s2)")
            },
            "Latitude"={
              selectList<- c(selectList,11)
              inputYAxis<-paste0(inputYAxis," (°N)")
            },
            "Longitude"={
              selectList<- c(selectList,10)
              inputYAxis<-paste0(inputYAxis," (°E)")
            },
            "Temperature"={
              e4selectList<- c(e4selectList,4)
              inputYAxis<-paste0(inputYAxis," (°C)")
            },
            "IBI"={
              e4selectList<- c(e4selectList,6)
              inputYAxis<-paste0(inputYAxis," (s)")
            },
            "BVP"=e4selectList<- c(e4selectList,5),
            "GSR"={
              e4selectList<- c(e4selectList,10)
              inputYAxis<-paste0(inputYAxis," (μS)")
            }
    )
    
    if(length(inputData[,1])!=0){
      #get data of selected users 
      inputData <- inputData[which(inputData[,1] %in% input$selectUser3_multi),]
      inputE4Data <- inputE4Data[which(inputE4Data[,1] %in% input$selectUser3_multi),]
      
      #if data table is null,occurs data type error in functions.R
      #make empty data table
      if(is.null(inputE4Data)){
        inputE4Data <- data.frame(NA,NA,NA,NA,NA,NA,NA,NA,NA,NA)
        colnames(inputE4Data)<-c("lungdata.productNo", "lungdata.Num","lungdata.sensingTime","lungdata.E4_temp", "lungdata.E4_bvp", "lungdata.E4_ibi", "lungdata.E4_accX","lungdata.E4_accY","lungdata.E4_accZ","lungdata.E4_gsr")
      }
      if(is.null(inputData)){
        inputData <- data.frame(NA,NA,NA,NA,NA,NA,NA,NA,NA,NA,NA)
        colnames(inputData)<-c("productNo","Num","sensingTime","acc_X","acc_Y","acc_Z","gyro_X","gyro_Y","gyro_Z","longitude","latitude")
      }
      #if y-axis is null, call defaultMultis function
      if(input$Y_Axis=="NULL"){
        if(input$X_Axis=="Time"){ 
          #if x-axis is time,
          # combine e4 time, and phone time
          inputtemp<-inputE4Data[,c(1,3)]
          colnames(inputtemp)<-c("productNo","sensingTime")
          inputs<-rbind(inputData[,c(1,3)],inputtemp)
          inputs <- inputs[c(order(inputs$sensingTime)),] #reorder by time
          ##### inputs <- unique(inputs)
          defaultMultis(inputs,inputXAxis,inputYAxis,input$selectGraphType2)
        }else{
          
          if(length(selectList)>length(e4selectList)){ #phone data
            inputs <-inputData[,selectList]
            inputs <- inputs[c(order(inputs[,2])),]
            defaultMultis(inputs,inputXAxis,inputYAxis,input$selectGraphType2)
          }else{ #e4 data
            inputs<-inputE4Data[,e4selectList]
            inputs<-inputs[c(order(inputs[,2])),]
            defaultMultis(inputs,inputXAxis,inputYAxis,input$selectGraphType2)
          }
          
        }
        
        
      }else{ # y-axis is not null
        #if lengths are equal of selectList(for phone), e4selectList (for e4)
        if(length(selectList)==length(e4selectList)){
          
          #if time-time, must be binding e4 time and phone time
          
          if(input$X_Axis == "Time" && input$Y_Axis=="Time"){ 
            tempE4Data <- inputE4Data[,c(1,3)]
            colnames(tempE4Data)<-c("productNo","sensingTime")
            
            inputs<-rbind(inputData[,c(1,3)],tempE4Data)
            colnames(inputs)<-c("productNo","sensingTime")
            
            inputs <- inputs[c(order(inputs$sensingTime)),]
            #### inputs <- unique(inputs)
            
            #second column and third column are equal
            inputs<- cbind(inputs,inputs[,2])
            colnames(inputs)<-c("productNo","Time","Time")
            defaultMulti2vars(inputs,inputXAxis,inputYAxis,input$selectGraphType)
          }else return(NULL)
        }else if(length(selectList)>length(e4selectList)){ #phone data
          inputs <-inputData[,selectList]
          inputs <- inputs[c(order(inputs[,2])),]
          defaultMulti2vars(inputs,inputXAxis,inputYAxis,input$selectGraphType)
        }else{ #e4 data
          inputs<-inputE4Data[,e4selectList]
          inputs<-inputs[c(order(inputs[,2])),]
          defaultMulti2vars(inputs,inputXAxis,inputYAxis,input$selectGraphType)
        }
      }
    }else{
      ggplot()+theme_bw()
    }
    
  }, height = 700)
  
  output$customize_graph_Indi <- renderPlot({
    inputData <- NULL
    inputE4Data<-NULL
    
    #input time
    startTime <- paste0(input$selectHour_s3_1,":",input$selectMin_s3_1,":",input$selectSec_s3_1)
    endTime <- paste0(input$selectHour_e3_1,":",input$selectMin_e3_1,":",input$selectSec_e3_1)
    i= c(strptime(paste(input$selectDate3_1[1],startTime),"%Y-%m-%d %H:%M:%S",tz="GMT-9"),strptime(paste(input$selectDate3_1[2],endTime),"%Y-%m-%d %H:%M:%S",tz="GMT-9"))
    
    #convert lungdata&e4Data sensingTime millisecond to second
    #for compare
    timeDate <- lungdata()
    timeDate$sensingTime <- as.POSIXct(lungdata()$sensingTime/1000,origin="1970-01-01",tz="GMT-9")
    timeE4Date <- e4Data()
    timeE4Date$lungdata.sensingTime <- as.POSIXct(e4Data()$lungdata.sensingTime/1000,origin="1970-01-01",tz="GMT-9")
    
    
    #check data in selected time is null
    if(length(which(i[1]<=timeDate$sensingTime  & timeDate$sensingTime <= i[2]))!=0) inputData <- lungdata()[which(i[1]<=timeDate$sensingTime  & timeDate$sensingTime <= i[2]),]
    else inputData<NULL
    if(length(which(i[1]<=timeE4Date$lungdata.sensingTime  & timeE4Date$lungdata.sensingTime <= i[2]))!=0) inputE4Data <- e4Data()[which(i[1]<=timeE4Date$lungdata.sensingTime  & timeE4Date$lungdata.sensingTime <= i[2]),]
    else inputE4Data<-NULL
    

    #if data table is null,occurs data type error in functions.R
    #make empty data table
    if(is.null(inputE4Data)){
      inputE4Data <- data.frame(NA,NA,NA,NA,NA,NA,NA,NA,NA,NA)
      colnames(inputE4Data)<-c("lungdata.productNo", "lungdata.Num","lungdata.sensingTime","lungdata.E4_temp", "lungdata.E4_bvp", "lungdata.E4_ibi", "lungdata.E4_accX","lungdata.E4_accY","lungdata.E4_accZ","lungdata.E4_gsr")
    }
    if(is.null(inputData)){
      inputData <- data.frame(NA,NA,NA,NA,NA,NA,NA,NA,NA,NA,NA)
      colnames(inputData)<-c("productNo","Num","sensingTime","acc_X","acc_Y","acc_Z","gyro_X","gyro_Y","gyro_Z","longitude","latitude")
    }
    
    #in individual mode, productNo(column number) not required
    selectList<-c()
    e4selectList<-c()
    
    #selected x-axis & y-axis input
    inputXAxis <-input$X_Axis
    inputYAxis<-input$Y_Axis
    
    switch (input$X_Axis,
            #there are two time (e4 time, phone time)
            "Time"={
              selectList<- c(selectList,3)
              e4selectList<-c(e4selectList,3)
              inputXAxis<-paste0(inputXAxis," (ms)") #adding units
            },
            "Accelerometer-X"={
              selectList<- c(selectList,4)
              inputXAxis<-paste0(inputXAxis," (m/s2)")
            },
            "Accelerometer-Y"={
              selectList<- c(selectList,5)
              inputXAxis<-paste0(inputXAxis," (m/s2)")
            },
            "Accelerometer-Z"={
              selectList<- c(selectList,6)
              inputXAxis<-paste0(inputXAxis," (m/s2)")
            },
            "Gyroscope-X"={
              selectList<- c(selectList,7)
              inputXAxis<-paste0(inputXAxis," (degree/sec)")
            },
            "Gyroscope-Y"={
              selectList<- c(selectList,8)
              inputXAxis<-paste0(inputXAxis," (degree/sec)")
            },
            "Gyroscope-Z"={
              selectList<- c(selectList,9)
              inputXAxis<-paste0(inputXAxis," (degree/sec)")
            },
            "E4Accelerometer-X"={
              e4selectList<- c(e4selectList,7)
              inputXAxis<-paste0(inputXAxis," (m/s2)")
            },
            "E4Accelerometer-Y"={
              e4selectList<- c(e4selectList,8)
              inputXAxis<-paste0(inputXAxis," (m/s2)")
            },
            "E4Accelerometer-Z"={
              e4selectList<- c(e4selectList,9)
              inputXAxis<-paste0(inputXAxis," (m/s2)")
            },
            "Latitude"={
              selectList<- c(selectList,11)
              inputXAxis<-paste0(inputXAxis," (°N)")
            },
            "Longitude"={
              selectList<- c(selectList,10)
              inputXAxis<-paste0(inputXAxis," (°E)")
            },
            "Temperature"={
              e4selectList<- c(e4selectList,4)
              inputXAxis<-paste0(inputXAxis," (°C)")
            },
            "IBI"={
              e4selectList<- c(e4selectList,6)
              inputXAxis<-paste0(inputXAxis," (s)")
            },
            "BVP"=e4selectList<- c(e4selectList,5),
            "GSR"={
              e4selectList<- c(e4selectList,10)
              inputXAxis<-paste0(inputXAxis," (μS)")
            }
    )
    switch (input$Y_Axis,
            "Time"={
              selectList<- c(selectList,3)
              e4selectList<-c(e4selectList,3)
              inputYAxis<-paste0(inputYAxis," (ms)")
            },
            "Accelerometer-X"={
              selectList<- c(selectList,4)
              inputYAxis<-paste0(inputYAxis," (m/s2)")
            },
            "Accelerometer-Y"={
              selectList<- c(selectList,5)
              inputYAxis<-paste0(inputYAxis," (m/s2)")
            },
            "Accelerometer-Z"={
              selectList<- c(selectList,6)
              inputYAxis<-paste0(inputYAxis," (m/s2)")
            },
            "Gyroscope-X"={
              selectList<- c(selectList,7)
              inputYAxis<-paste0(inputYAxis," (degree/sec)")
            },
            "Gyroscope-Y"={
              selectList<- c(selectList,8)
              inputYAxis<-paste0(inputYAxis," (degree/sec)")
            },
            "Gyroscope-Z"={
              selectList<- c(selectList,9)
              inputYAxis<-paste0(inputYAxis," (degree/sec)")
            },
            "E4Accelerometer-X"={
              e4selectList<- c(e4selectList,7)
              inputYAxis<-paste0(inputYAxis," (m/s2)")
            },
            "E4Accelerometer-Y"={
              e4selectList<- c(e4selectList,8)
              inputYAxis<-paste0(inputYAxis," (m/s2)")
            },
            "E4Accelerometer-Z"={
              e4selectList<- c(e4selectList,9)
              inputYAxis<-paste0(inputYAxis," (m/s2)")
            },
            "Latitude"={
              selectList<- c(selectList,11)
              inputYAxis<-paste0(inputYAxis," (°N)")
            },
            "Longitude"={
              selectList<- c(selectList,10)
              inputYAxis<-paste0(inputYAxis," (°E)")
            },
            "Temperature"={
              e4selectList<- c(e4selectList,4)
              inputYAxis<-paste0(inputYAxis," (°C)")
            },
            "IBI"={
              e4selectList<- c(e4selectList,6)
              inputYAxis<-paste0(inputYAxis," (s)")
            },
            "BVP"=e4selectList<- c(e4selectList,5),
            "GSR"={
              e4selectList<- c(e4selectList,10)
              inputYAxis<-paste0(inputYAxis," (μS)")
            }
    )
    if(length(inputData[,1])!=0){
      #get data of selected user (one user)
      inputData <- inputData[which(inputData[,1]==input$selectUser3_Individual),]
      inputE4Data <- inputE4Data[which(inputE4Data[,1]==input$selectUser3_Individual),]
      
      #y axis is null call function defaults in functions.R
      if(input$Y_Axis=="NULL") {
        if(input$X_Axis=="Time"){
          #if y-axis is null and, x axis is time
          #must be binding e4 time and phone time
          inputs= c(inputData[,3],inputE4Data[which(!is.na(inputE4Data[,3])),3])
          #### inputs <- unique(inputs[order(inputs)])
          inputs <- inputs[order(inputs)]
          defaults(inputs,inputXAxis,inputYAxis,input$selectGraphType2_1)
        }else {
         
          if(length(selectList)>length(e4selectList)){#phone data
            inputs <-inputData[,selectList]
            inputs <- inputs[c(order(inputs))]
            defaults(inputs,inputXAxis,inputYAxis,input$selectGraphType2_1)
          }else { #e4 data
            inputs<-inputE4Data[,e4selectList]
            inputs<-inputs[c(order(inputs))]
            defaults(inputs,inputXAxis,inputYAxis,input$selectGraphType2_1)
          }
        }
        
      }else {  #y axis is not null call function default2vars in functions.R
        if(length(selectList)==length(e4selectList)){
          if(input$X_Axis == "Time" && input$Y_Axis=="Time"){
            #x, y-axis are time, required binding
            inputs= c(inputData[,3],inputE4Data[,3])
            #### inputs <- unique(inputs[c(order(inputs))])
            inputs <- inputs[c(order(inputs))]
            inputs<- cbind(inputs,inputs)
            default2vars(inputs,inputXAxis,inputYAxis,input$selectGraphType_1)
          }else return(NULL)
          
        }else if(length(selectList)>length(e4selectList)){#phone data
          inputs <-inputData[,selectList]
          inputs <- inputs[c(order(inputs[,1])),]
          
          default2vars(inputs,inputXAxis,inputYAxis,input$selectGraphType_1)
        }else{ #e4 data
          inputs<-inputE4Data[,e4selectList]
          inputs<-inputs[c(order(inputs[,1])),]
          default2vars(inputs,inputXAxis,inputYAxis,input$selectGraphType_1)
        }
      }
      
      #data table is emoty, blank graph
    }else{
      ggplot()+theme_bw()
    }
    
  }, height = 700)
  
}





