library(shiny)
library(ggplot2)
library(ggvis)
library(reshape)
library(gridExtra)

#for Large Data
options("scipen"=100)
options(digits = 13)

#entireData contains user phone number, date format fileName, and path of the fileName.
entireData<- data.frame()
#get files in phone number format
usersName<- list.files(path ="./inSeq",pattern = "^\\d{3}-\\d{3,4}-\\d{4}$", full.names = F)

for(var in usersName){
  #get path of each file
  usersPath <- list.files(path =paste0("./inSeq/",var),pattern = "[0-9]", full.names = T)
  userFile <- list.files(path =paste0("./inSeq/",var),pattern = "[0-9]", full.names = F)
  
  du <-c()
  du[1:length(userFile)]=var
  
  tempTable <- data.frame(du,userFile,usersPath)
  colnames(tempTable)<-c("udid","fileName","Path")
  
  entireData<-rbind(entireData,tempTable)
}
#users is list of phone number
users <- unique(usersName)

#for customize version graph
inputXAxis <- c("Time","Accelerometer-X","Accelerometer-Y","Accelerometer-Z","Gyroscope-X","Gyroscope-Y","Gyroscope-Z","E4Accelerometer-X","E4Accelerometer-Y","E4Accelerometer-Z","Latitude","Longitude","Temperature","IBI","BVP","GSR")
inputYAxis <- c("NULL","Time","Accelerometer-X","Accelerometer-Y","Accelerometer-Z","Gyroscope-X","Gyroscope-Y","Gyroscope-Z","E4Accelerometer-X","E4Accelerometer-Y","E4Accelerometer-Z","Latitude","Longitude","Temperature","IBI","BVP","GSR")


#Phone & E4 Accelerometer Data
#default graph
getAcceleromter <- function(inputData,inputE4Data,n){
  #input n is user number (phone number)
  file <- inputData[which(inputData[,1]==n),]
  reg <- file[,3]
  
  file_acc <- data.frame(reg, file$acc_X,file$acc_Y,file$acc_Z)
  
  #if all of axis data is null, show blank graph
  if(length(which(!is.na(file_acc[,c(2:4)])))!=0){
    
    #x-axis label of graph
    timeIntervaltemp = as.numeric(file_acc$reg[length(file_acc$reg)]-file_acc$reg[1])/7
    timeInterval<- seq(file_acc$reg[1],file_acc$reg[length(file_acc$reg)],timeIntervaltemp)
    
    #y-axis label of graph
    accMax = max(c(max(file_acc$file.acc_X[which(is.na(file_acc$file.acc_X)==F)]),max(file_acc$file.acc_Y[which(is.na(file_acc$file.acc_Y)==F)]),max(file_acc$file.acc_Z[which(is.na(file_acc$file.acc_Z)==F)])))
    accMin = min(c(min(file_acc$file.acc_X[which(is.na(file_acc$file.acc_X)==F)]),min(file_acc$file.acc_Y[which(is.na(file_acc$file.acc_Y)==F)]),min(file_acc$file.acc_Z[which(is.na(file_acc$file.acc_Z)==F)])))
    accIn <- (accMax-accMin)/10
    accseq<-seq(accMin,accMax,accIn)
    
    #draw graph
    #organize data according to reg(time)
    #other values are taken as the y-axis value.
    accGraph <-ggplot(data = melt(file_acc, id.var="reg"), mapping=aes(reg, value))+geom_line(aes(colour=variable, group=variable),cex=0.4)
    accGraph <- accGraph + theme_bw()
    accGraph <- accGraph+scale_x_continuous(breaks = timeInterval)
    accGraph <- accGraph+scale_y_continuous(breaks = accseq,labels =sprintf("%0.5f",round(accseq,digits = 5)))
    accGraph <-accGraph+labs(x="Time (ms)", y="Acceleration Value (m/s2)")
    accGraph <-accGraph+theme(axis.title = element_text(face = "plain",hjust = 0.5, size=11))
    accGraph <-accGraph+ggtitle("Phone Accelerometer Sensing Data")
    accGraph <- accGraph+theme(plot.title = element_text(family="serif", face = "bold",hjust = 0.5, size=18, color="darkred"))
    accGraph <- accGraph + guides(color=guide_legend(title = NULL))
    accGraph <- accGraph + scale_color_discrete(labels=c("X-Axis","Y-Axis","Z-Axis"))
  }
  else{ #draw blank graph
    accGraph <-ggplot()
    accGraph <- accGraph + theme_bw()
    accGraph <-accGraph+labs(x="Time (ms)", y="Acceleration Value (m/s2)")
    accGraph <-accGraph+theme(axis.title = element_text(face = "plain",hjust = 0.5, size=11))
    accGraph <-accGraph+ggtitle("Phone Accelerometer Sensing Data")
    accGraph <- accGraph+theme(plot.title = element_text(family="serif", face = "bold",hjust = 0.5, size=18, color="darkred"))
    accGraph <- accGraph + guides(color=guide_legend(title = NULL))
    accGraph <- accGraph + scale_color_discrete(labels=c("X-Axis","Y-Axis","Z-Axis"))
  }
  
  #e4 data
  #input n is user number (phone number)
  file <- inputE4Data[which(inputE4Data[,1]==n),]
  colnames(file)<-c("productNo","Num","sensingTime","E4_temp","E4_bvp","E4_ibi","E4_accX","E4_accY","E4_accZ","E4_gsr")
  
  reg <- file[,3]
  #organize data 
  file_accE4 <- data.frame(reg, file$E4_accX,file$E4_accY,file$E4_accZ)
  
  
  if(length(which(!is.na(file_accE4[,c(2:4)])))!=0){
    #x-axis label of graph
    timeIntervaltemp = as.numeric(file_accE4$reg[length(file_accE4$reg)]-file_accE4$reg[1])/7
    timeInterval<- seq(file_accE4$reg[1],file_accE4$reg[length(file_accE4$reg)],timeIntervaltemp)
    #y-axis label of graph
    accE4Max = max(c(max(file_accE4$file.E4_accX[!is.na(file_accE4$file.E4_accX)]),max(file_accE4$file.E4_accY[!is.na(file_accE4$file.E4_accY)]),max(file_accE4$file.E4_accZ[!is.na(file_accE4$file.E4_accZ)])))
    accE4Min = min(c(min(file_accE4$file.E4_accX[which(is.na(file_accE4$file.E4_accX)==F)]),min(file_accE4$file.E4_accY[which(is.na(file_accE4$file.E4_accY)==F)]),min(file_accE4$file.E4_accZ[which(is.na(file_accE4$file.E4_accZ)==F)])))
    accE4In <- (accE4Max-accE4Min)/10
    accE4seq<-seq(accE4Min,accE4Max,accE4In)
    
    #draw graph
    #organize data according to reg, and other values are taken as the y-axis value.
    #line color is displayed according to the other values(x-axis, y-axis, z-axis) 
    accE4Graph <-ggplot(data = melt(file_accE4, id.var="reg"), mapping=aes(reg, value))+geom_line(aes(colour=variable, group=variable),cex=0.4)
    accE4Graph <- accE4Graph + theme_bw()
    accE4Graph <- accE4Graph+scale_x_continuous(breaks = timeInterval)
    accE4Graph <- accE4Graph+scale_y_continuous(breaks =accE4seq ,labels =sprintf("%0.5f",round(accE4seq,digits = 5)))
    accE4Graph <-accE4Graph+labs(x="Time (ms)", y="E4 Acceleration Value (m/s2)")
    accE4Graph <-accE4Graph+theme(axis.title = element_text(face = "plain",hjust = 0.5, size=11))
    accE4Graph <-accE4Graph+ggtitle("E4 Accelerometer Sensing Data")
    accE4Graph <- accE4Graph+theme(plot.title = element_text(family="serif", face = "bold",hjust = 0.5, size=18, color="darkblue"))
    accE4Graph <- accE4Graph + guides(color=guide_legend(title = NULL))
    accE4Graph <- accE4Graph + scale_color_discrete(labels=c("X-Axis","Y-Axis","Z-Axis"))
  }
  else{
    #blank graph
    accE4Graph <-ggplot()
    accE4Graph <- accE4Graph + theme_bw()
    accE4Graph <-accE4Graph+labs(x="Time (ms)", y="E4 Acceleration Value (m/s2)")
    accE4Graph <-accE4Graph+theme(axis.title = element_text(face = "plain",hjust = 0.5, size=11))
    accE4Graph <-accE4Graph+ggtitle("E4 Accelerometer Sensing Data")
    accE4Graph <- accE4Graph+theme(plot.title = element_text(family="serif", face = "bold",hjust = 0.5, size=18, color="darkblue"))
    accE4Graph <- accE4Graph + scale_color_discrete(labels=c("X-Axis","Y-Axis","Z-Axis"))
  }
  #return two graph
  grid.arrange(accGraph,accE4Graph,nrow=2)
}

#Phone Gyroscope Data
#default graph
getGyro <- function(inputData,n){
  
  #input n is user number (phone number)
  file <- inputData[which(inputData[,1]==n),]
  reg <- file[,3]
  file_gyro <- data.frame(reg,file$gyro_X,file$gyro_Y,file$gyro_Z)
  
  if(length(which(!is.na(file_gyro[,c(2:4)])))!=0){
    #x-axis label of graph
    timeIntervaltemp = as.numeric(file_gyro$reg[length(file_gyro$reg)]-file_gyro$reg[1])/7
    timeInterval<- seq(file_gyro$reg[1],file_gyro$reg[length(file_gyro$reg)],timeIntervaltemp)
    
    
    #y-axis label of graph 
    gyroMax = max(c(max(file_gyro$file.gyro_X[which(is.na(file_gyro$file.gyro_X)==F)]),max(file_gyro$file.gyro_Y[which(is.na(file_gyro$file.gyro_Y)==F)]),max(file_gyro$file.gyro_Z[which(is.na(file_gyro$file.gyro_Z)==F)])))
    gyroMin = min(c(min(file_gyro$file.gyro_X[which(is.na(file_gyro$file.gyro_X)==F)]),min(file_gyro$file.gyro_Y[which(is.na(file_gyro$file.gyro_Y)==F)]),min(file_gyro$file.gyro_Z[which(is.na(file_gyro$file.gyro_Z)==F)])))
    gyroIn <- (gyroMax-gyroMin)/10
    gyroseq<-seq(gyroMin,gyroMax,gyroIn)
    
    #draw graph
    #organize data according to reg, and other values are taken as the y-axis value.
    #line color is displayed according to the other values(x-axis, y-axis, z-axis) 
    gyroGraph <-ggplot(data = melt(file_gyro, id.var="reg"), mapping=aes(reg, value))+geom_line(aes(colour=variable, group=variable),cex=0.4)
    gyroGraph <- gyroGraph + theme_bw()
    gyroGraph <- gyroGraph+scale_x_continuous(breaks = timeInterval)
    gyroGraph <- gyroGraph+scale_y_continuous(breaks = gyroseq,labels =sprintf("%0.5f",round(gyroseq,digits = 5)))
    gyroGraph <-gyroGraph+labs(x="Time (ms)", y="Gyroscope Value (degree/sec)")
    gyroGraph <-gyroGraph+theme(axis.title = element_text(face = "plain",hjust = 0.5, size=11))
    gyroGraph <-gyroGraph+ggtitle("GyroScope Sensing Data")
    gyroGraph <- gyroGraph+theme(plot.title = element_text(family="serif", face = "bold",hjust = 0.5, size=18, color="darkblue"))
    gyroGraph <- gyroGraph + guides(color=guide_legend(title = NULL))
    gyroGraph <- gyroGraph + scale_color_discrete(labels=c("X-Axis","Y-Axis","Z-Axis"))
  }
  else{
    
    #blank graph
    gyroGraph <-ggplot()
    gyroGraph <- gyroGraph + theme_bw()
    gyroGraph <-gyroGraph+labs(x="Time (ms)", y="Gyroscope Value (degree/sec)")
    gyroGraph <-gyroGraph+theme(axis.title = element_text(face = "plain",hjust = 0.5, size=11))
    gyroGraph <-gyroGraph+ggtitle("GyroScope Sensing Data")
    gyroGraph <- gyroGraph+theme(plot.title = element_text(family="serif", face = "bold",hjust = 0.5, size=18, color="darkblue"))
    gyroGraph <- gyroGraph + guides(color=guide_legend(title = NULL))
    gyroGraph <- gyroGraph + scale_color_discrete(labels=c("X-Axis","Y-Axis","Z-Axis"))
  }
  
  #return gyroscope graph
  gyroGraph
}

#E4 Temperature Data
#temperature default graph
getTemp <- function(inputData,n){
  
  #input n is user number (phone number)
  file <- inputData[which(inputData[,1]==n),]
  reg <- file[,3]
  #organize data with time, temperature
  file_temp <- data.frame(reg,file$lungdata.E4_temp)
  
  timeIntervaltemp = as.numeric(file_temp$reg[length(file_temp$reg)]-file_temp$reg[1])/7
  timeInterval<- ifelse(length(file_temp$reg)==0, 0, seq(file_temp$reg[1],file_temp$reg[length(file_temp$reg)],timeIntervaltemp))
  
  if(length(which(!is.na(file_temp[,2])))!=0){
    
    # x-axis label of graph
    timeIntervaltemp = as.numeric(file_temp$reg[length(file_temp$reg)]-file_temp$reg[1])/7
    timeInterval<- seq(file_temp$reg[1],file_temp$reg[length(file_temp$reg)],timeIntervaltemp)
    # y-axis label of graph
    tempMax = max(file_temp$file.lungdata.E4_temp[which(is.na(file_temp$file.lungdata.E4_temp)==F)])
    tempMin = min(file_temp$file.lungdata.E4_temp[which(is.na(file_temp$file.lungdata.E4_temp)==F)])
    tempIn <- (tempMax-tempMin)/10
    tempseq<-seq(tempMin,tempMax,tempIn)
    
    #draw graph
    tempGraph <-ggplot(data = melt(file_temp, id.var="reg"), mapping=aes(reg, value))+geom_line(colour="red",cex=0.4)
    tempGraph <- tempGraph + theme_bw()
    tempGraph <- tempGraph+scale_x_continuous(breaks = timeInterval)
    tempGraph <- tempGraph+scale_y_continuous(breaks = tempseq,labels =sprintf("%0.5f",round(tempseq,digits = 5)))
    tempGraph <-tempGraph+labs(x="Time (ms)", y="E4 Temperature (°C)")
    tempGraph <- tempGraph+theme(axis.title = element_text(face = "plain",hjust = 0.5, size=11))
    tempGraph <-tempGraph+ggtitle("E4 Temperature Sensing Data")
    tempGraph <-tempGraph+theme(plot.title = element_text(family="serif", face = "bold",hjust = 0.5, size=18, color="darkblue"))
  }
  else{
    #blank graph
    tempGraph <-ggplot()
    tempGraph <- tempGraph + theme_bw()
    tempGraph <-tempGraph+labs(x="Time (ms)", y="E4 Temperature (°C)")
    tempGraph <- tempGraph+theme(axis.title = element_text(face = "plain",hjust = 0.5, size=11))
    tempGraph <-tempGraph+ggtitle("E4 Temperature Sensing Data")
    tempGraph <-tempGraph+theme(plot.title = element_text(family="serif", face = "bold",hjust = 0.5, size=18, color="darkblue"))
  }
  #return temperature graph
  tempGraph
  
}
#ibi default graph
getIBI <- function(inputData,n){
  
  #input n is user number (phone number)
  file <- inputData[which(inputData[,1]==n),]
  reg <- file[,3]
  #organize data with time, ibi value
  file_ibi <- data.frame(reg,file$lungdata.E4_ibi)
  
  
  if(length(which(!is.na(file_ibi[,2])))!=0){
    
    #x-axis label of graph
    timeIntervaltemp = as.numeric(file_ibi$reg[length(file_ibi$reg)]-file_ibi$reg[1])/7
    timeInterval<- seq(file_ibi$reg[1],file_ibi$reg[length(file_ibi$reg)],timeIntervaltemp)
    
    #y-axis label of graph
    ibiMax = max(file_ibi$file.lungdata.E4_ibi[which(is.na(file_ibi$file.lungdata.E4_ibi)==F)])
    ibiMin = min(file_ibi$file.lungdata.E4_ibi[which(is.na(file_ibi$file.lungdata.E4_ibi)==F)])
    ibiIn <- (ibiMax-ibiMin)/10
    ibiseq<-seq(ibiMin,ibiMax,ibiIn)
    
    #draw graph
    ibiGraph <-ggplot(data = melt(file_ibi, id.var="reg"), mapping=aes(reg, value))+geom_line(colour="#04B431",cex=0.4)
    ibiGraph <- ibiGraph + theme_bw()
    ibiGraph <- ibiGraph+scale_x_continuous(breaks = timeInterval)#, labels = as.POSIXct(timeInterval, origin = "1970-01-01"))
    ibiGraph <- ibiGraph+scale_y_continuous(breaks = ibiseq,labels =sprintf("%0.5f",round(ibiseq,digits = 5)))
    ibiGraph <-ibiGraph+labs(x="Time (ms)", y="E4 IBI (s)")
    ibiGraph <- ibiGraph+theme(axis.title = element_text(face = "plain",hjust = 0.5, size=11))
    ibiGraph <-ibiGraph+ggtitle("Inter Beat Interval \n Sensing Data")
    ibiGraph <-ibiGraph+theme(plot.title = element_text(family="serif", face = "bold",hjust = 0.5, size=18, color="darkblue"))
    ibiGraph #return ibi graph
  }
  else{
    #blank graph
    ibiGraph <-ggplot()
    ibiGraph <- ibiGraph + theme_bw()
    ibiGraph <-ibiGraph+labs(x="Time (ms)", y="E4 IBI (s)")
    ibiGraph <- ibiGraph+theme(axis.title = element_text(face = "plain",hjust = 0.5, size=11))
    ibiGraph <-ibiGraph+ggtitle("Inter Beat Interval \n Sensing Data")
    ibiGraph <-ibiGraph+theme(plot.title = element_text(family="serif", face = "bold",hjust = 0.5, size=18, color="darkblue"))
    ibiGraph #return ibi graph
  }
  
}

#bvp default graph
getBVP <- function(inputData,n){
  #input n is user number (phone number)
  file <- inputData[which(inputData[,1]==n),]
  reg <- file[,3]
  #organize data with time, bvp value
  file_bvp <- data.frame(reg,file$lungdata.E4_bvp)
  
  
  if(length(which(!is.na(file_bvp[,2])))!=0){
    #x-axis label of graph
    timeIntervaltemp = as.numeric(file_bvp$reg[length(file_bvp$reg)]-file_bvp$reg[1])/7
    timeInterval<- seq(file_bvp$reg[1],file_bvp$reg[length(file_bvp$reg)],timeIntervaltemp)
    
    #y-axis label of graph
    bvpMax = max(file_bvp$file.lungdata.E4_bvp[which(is.na(file_bvp$file.lungdata.E4_bvp)==F)])
    bvpMin = min(file_bvp$file.lungdata.E4_bvp[which(is.na(file_bvp$file.lungdata.E4_bvp)==F)])
    bvpIn <-(bvpMax-bvpMin)/10
    bvpseq<- seq(bvpMin,bvpMax,bvpIn)
    
    #draw graph
    bvpGraph <-ggplot(data = melt(file_bvp, id.var="reg"), mapping=aes(reg, value))+geom_line(colour="black",cex=0.4)
    bvpGraph <- bvpGraph + theme_bw()
    bvpGraph <- bvpGraph+scale_x_continuous(breaks = timeInterval)
    bvpGraph <- bvpGraph+scale_y_continuous(breaks =bvpseq,labels = sprintf("%0.5f",round(bvpseq,digits = 5)))
    bvpGraph <-bvpGraph+labs(x="Time (ms)", y="E4 BVP")
    bvpGraph <- bvpGraph+theme(axis.title = element_text(face = "plain",hjust = 0.5, size=11))
    bvpGraph <-bvpGraph+ggtitle("Blood Volume Pulse Sensing Data")
    bvpGraph <-bvpGraph+theme(plot.title = element_text(family="serif", face = "bold",hjust = 0.5, size=18, color="darkblue"))
    bvpGraph #return bvp graph
  }
  else{
    
    #blank graph
    bvpGraph <-ggplot()
    bvpGraph <- bvpGraph + theme_bw()
    bvpGraph <-bvpGraph+labs(x="Time (ms)", y="E4 BVP")
    bvpGraph <-bvpGraph+ggtitle("Blood Volume Pulse Sensing Data")
    bvpGraph <-bvpGraph+theme(plot.title = element_text(family="serif", face = "bold",hjust = 0.5, size=18, color="darkblue"))
    bvpGraph #return bvp graph
  }
  
}
#gsr default graph
getGSR <- function(inputData,n){
  #input n is user phone number,
  file <- inputData[which(inputData[,1]==n),]
  reg <- file[,3]
  #organize data with time, gsr value
  file_gsr <- data.frame(reg,file$lungdata.E4_gsr)
  
  if(length(which(!is.na(file_gsr[,2])))!=0){
    #x-axis label of graph
    timeIntervaltemp = as.numeric(file_gsr$reg[length(file_gsr$reg)]-file_gsr$reg[1])/7
    timeInterval<- seq(file_gsr$reg[1],file_gsr$reg[length(file_gsr$reg)],timeIntervaltemp)
    #y-axis label of graph
    gsrMax = max(file_gsr$file.lungdata.E4_gsr[which(is.na(file_gsr$file.lungdata.E4_gsr)==F)])
    gsrMin = min(file_gsr$file.lungdata.E4_gsr[which(is.na(file_gsr$file.lungdata.E4_gsr)==F)])
    gsrIn <- (gsrMax-gsrMin)/10
    gsrseq<-seq(gsrMin,gsrMax,gsrIn)
    
    #draw graph
    gsrGraph <-ggplot(data = melt(file_gsr, id.var="reg"), mapping=aes(reg, value))+geom_line(colour="#0B6121",cex=0.4)
    gsrGraph <- gsrGraph + theme_bw()
    gsrGraph <- gsrGraph+scale_x_continuous(breaks = timeInterval)
    gsrGraph <- gsrGraph+scale_y_continuous(breaks =gsrseq,labels = sprintf("%0.5f",round(gsrseq,digits = 5)))
    gsrGraph <-gsrGraph+labs(x="Time (ms)",y="E4 GSR (μS)")
    gsrGraph <- gsrGraph+theme(axis.title = element_text(face = "plain",hjust = 0.5, size=11))
    gsrGraph <-gsrGraph+ggtitle("Galvanic Skin Reflex Sensing Data")
    gsrGraph <-gsrGraph+theme(plot.title = element_text(family="serif", face = "bold",hjust = 0.5, size=18, color="darkblue"))
  }
  else{
    #blank graph
    gsrGraph <-ggplot()
    gsrGraph <- gsrGraph + theme_bw()
    gsrGraph <-gsrGraph+labs(x="Time (ms)",y="E4 GSR (μS)")
    gsrGraph <- gsrGraph+theme(axis.title = element_text(face = "plain",hjust = 0.5, size=11))
    gsrGraph <-gsrGraph+ggtitle("Galvanic Skin Reflex Sensing Data")
    gsrGraph <-gsrGraph+theme(plot.title = element_text(family="serif", face = "bold",hjust = 0.5, size=18, color="darkblue"))
  }
  #return gsr graph
  gsrGraph
}

#default location graph
getLocation <- function(inputData){
  #organize data with phone number, longitude, latitude
  file_loc = data.frame(inputData$productNo,inputData$latitude, inputData$longitude)
  
  if(length(which(!is.na(file_loc[,2])))!=0){
    
    #draw graph
    #organize data according to latidue,longitude
    #mapping x-axis to longitude, y-axis to latitude
    #color of points is displayed according to phone number
    locGraph <-ggplot(data = melt(file_loc, id.vars=c("inputData.latitude","inputData.longitude")), mapping=aes(x=inputData.longitude,y=inputData.latitude ,value))+geom_point(aes(colour=factor(value), group=factor(value)))#+geom_line(aes(colour=factor(value), group=factor(value)))
    locGraph <- locGraph + theme_bw()
    locGraph <-locGraph+labs(x="Longitude (°E)",y="Latitude (°N)")
    locGraph <-locGraph+theme(axis.title = element_text(face = "plain",hjust = 0.5, size=11))
    locGraph <-locGraph+ggtitle("Location Data of Entire User")
    locGraph <- locGraph+theme(plot.title = element_text(family="serif", face = "bold",hjust = 0.5, size=18, color="#8904B1"))
    locGraph <- locGraph + guides(color=guide_legend(title = NULL))
  }else{
    #blank graph
    locGraph <-ggplot()
    locGraph <- locGraph + theme_bw()
    locGraph <-locGraph+labs(x="Longitude (°E)",y="Latitude (°N)")
    locGraph <-locGraph+theme(axis.title = element_text(face = "plain",hjust = 0.5, size=11))
    locGraph <-locGraph+ggtitle("Location Data of Entire User")
    locGraph <- locGraph+theme(plot.title = element_text(family="serif", face = "bold",hjust = 0.5, size=18, color="#8904B1"))
  }
  #return location graph
  locGraph
}

#customize graph of multi version, if y-axis is null
defaultMultis <-function(inputData, inputXAxis, inputYAxis, graphType){
  
  file<- inputData
  
  if(length(which(!is.na(file[,2])))!=0){
    if(inputXAxis != "phone"){
      #rename column
      colnames(file)<- c("phone","inputXAxis")
      
      #x-axis label of graph
      Intervaltemp = as.numeric(max(file[which(!is.na(file[,2])),2])-min(file[which(!is.na(file[,2])),2]))/8
      Interval<- seq(min(file[which(!is.na(file[,2])),2]),max(file[which(!is.na(file[,2])),2]),Intervaltemp)
      
      #for binwidth
      width<- as.numeric(max(file[which(!is.na(file[,2])),2])-min(file[which(!is.na(file[,2])),2]))/10
      
      #draw graph
      returnGraph <-ggplot(data = file, mapping=aes(file[,2]))
      returnGraph <- returnGraph + theme_bw()
      returnGraph <- returnGraph+scale_x_continuous(breaks = Interval)
      returnGraph <- returnGraph+labs(x=inputXAxis)
      returnGraph <-returnGraph+theme(axis.title = element_text(face = "plain",hjust = 0.5, size=11))
      returnGraph <-returnGraph+ggtitle("Customized Graph")
      returnGraph <- returnGraph+theme(plot.title = element_text(family="serif", face = "bold",hjust = 0.5, size=18, color="darkblue"))
      
      #draw according to graphType
      switch (graphType,
              "area"= returnGraph<-returnGraph+geom_area(aes(fill=factor(file$phone),colour=factor(file$phone)),stat = "bin",binwidth=width),
              "density"=returnGraph<-returnGraph+geom_density(aes(colour=factor(file$phone)),kernel="gaussian"),
              "dotplot"=returnGraph<-returnGraph+geom_dotplot(aes(fill=factor(file$phone),colour=factor(file$phone)),binwidth = width),
              "freqpoly"= returnGraph<-returnGraph+geom_freqpoly(aes(colour=factor(file$phone)),binwidth=width),
              "histogram"=returnGraph<-returnGraph+geom_histogram(aes(fill=factor(file$phone)),binwidth =width)
      )
      #return graph
      returnGraph
    }
  }
  else{
    #blank graph
    returnGraph <-ggplot()+geom_blank()
    returnGraph <- returnGraph + theme_bw()
    returnGraph <- returnGraph+labs(x=NULL, y=NULL)
    returnGraph <-returnGraph+theme(axis.title = element_text(face = "plain",hjust = 0.5, size=11))
    returnGraph <-returnGraph+ggtitle("Customized Graph")
    returnGraph <- returnGraph+theme(plot.title = element_text(family="serif", face = "bold",hjust = 0.5, size=18, color="darkblue"))
    #return graph
    returnGraph
  }
  
}

#customize graph of multi version ~ y-axis is not null
defaultMulti2vars <-function(inputData, inputXAxis, inputYAxis, graphType){
  
  file<-inputData
  
  if(length(which(!is.na(file[,3])))!=0){
    #rename column
    colnames(file)<- c("phone","inputXAxis","inputYAxis")
    
    #x-axis label of graph
    Intervaltemp = as.numeric(max(file[which(!is.na(file[,2])),2])-min(file[which(!is.na(file[,2])),2]))/8
    Interval<- seq(min(file[which(!is.na(file[,2])),2]),max(file[which(!is.na(file[,2])),2]),Intervaltemp)
    #y-axis label of graph
    maxValue = max(file[which(!is.na(file[,3])),3])
    minValue = min(file[which(!is.na(file[,3])),3])
    intervalValue <- (maxValue-minValue)/10
    intervalseq<-seq(minValue,maxValue,intervalValue)
    
    #draw graph
    #mapping x-axis to second column, y-axis to third column
    returnGraph <-ggplot(data = file, mapping=aes(file[,2], file[,3]))
    returnGraph <- returnGraph + theme_bw()
    returnGraph <- returnGraph+scale_x_continuous(breaks = Interval)
    returnGraph <- returnGraph+scale_y_continuous(breaks = intervalseq,labels =sprintf("%0.5f",round(intervalseq,digits = 5)))
    returnGraph <- returnGraph+labs(x=inputXAxis, y=inputYAxis)
    returnGraph <-returnGraph+theme(axis.title = element_text(face = "plain",hjust = 0.5, size=11))
    returnGraph <-returnGraph+ggtitle("Customized Graph")
    returnGraph <- returnGraph+theme(plot.title = element_text(family="serif", face = "bold",hjust = 0.5, size=18, color="darkblue"))
    #draw according to graphType
    #color of graph is displayed according to phone number
    switch (graphType,
            "line" = returnGraph<- returnGraph+geom_line(aes(colour=factor(file$phone)),cex=0.4),
            "point" = returnGraph<- returnGraph+geom_point(aes(colour=factor(file$phone)),size=0.4),
            "step"= returnGraph<- returnGraph+geom_step(aes(colour=factor(file$phone)),direction = "hv"),
            "rug"=returnGraph<- returnGraph+geom_rug(aes(colour=factor(file$phone)),sides = "bl")+geom_line(),
            "smooth"=returnGraph<- returnGraph+geom_smooth(aes(colour=factor(file$phone))),
            "text"=returnGraph<- returnGraph+geom_text(aes(label=file[,1],colour=factor(file$phone)), nudge_x = 1, nudge_y = 1, check_overlap = T),
            "label"=returnGraph<- returnGraph+geom_label(aes(label=file[,1],colour=factor(file$phone)), nudge_x = 1, nudge_y = 1, check_overlap = T),
            "jitter"=returnGraph<- returnGraph+geom_jitter(aes(colour=factor(file$phone)),height = 2,width = 2)
    )
    #return graph
    returnGraph
  }
  
  else{
    #blank graph
    returnGraph <-ggplot()+geom_blank()
    returnGraph <- returnGraph + theme_bw()
    returnGraph <- returnGraph+labs(x=NULL, y=NULL)
    returnGraph <-returnGraph+theme(axis.title = element_text(face = "plain",hjust = 0.5, size=11))
    returnGraph <-returnGraph+ggtitle("Customized Graph")
    returnGraph <- returnGraph+theme(plot.title = element_text(family="serif", face = "bold",hjust = 0.5, size=18, color="darkblue"))
    returnGraph
  }
  
}

#customize graph of Individual version ~ y-axis is not null
default2vars <-function(inputData, inputXAxis, inputYAxis, graphType){
  
  file<-inputData
  if(length(which(!is.na(file[,2])))!=0){
    #rename column
    colnames(file)<- c("inputXAxis","inputYAxis")
    #x-axis label of graph
    Intervaltemp = as.numeric(max(file[which(!is.na(file[,1])),1])-min(file[which(!is.na(file[,1])),1]))/8
    Interval<- seq(min(file[which(!is.na(file[,1])),1]),max(file[which(!is.na(file[,1])),1]),Intervaltemp)
    #y-axis label of graph
    maxValue = max(file[which(!is.na(file[,2])),2])
    minValue = min(file[which(!is.na(file[2])),2])
    intervalValue <- (maxValue-minValue)/10
    intervalseq<-seq(minValue,maxValue,intervalValue)
    
    #draw graph
    #mapping x-axis to first column, y-axis to second column
    returnGraph <-ggplot(data = file, mapping=aes(file[,1], file[,2]))
    returnGraph <- returnGraph + theme_bw()
    returnGraph <- returnGraph+scale_x_continuous(breaks = Interval)
    returnGraph <- returnGraph+scale_y_continuous(breaks = intervalseq,labels =sprintf("%0.5f",round(intervalseq,digits = 5)))
    returnGraph <- returnGraph+labs(x=inputXAxis, y=inputYAxis)
    returnGraph <-returnGraph+theme(axis.title = element_text(face = "plain",hjust = 0.5, size=11))
    returnGraph <-returnGraph+ggtitle("Customized Graph")
    returnGraph <- returnGraph+theme(plot.title = element_text(family="serif", face = "bold",hjust = 0.5, size=18, color="darkblue"))
    #draw according to graphType
    switch (graphType,
            "line" = returnGraph<- returnGraph+geom_line(colour="#04B431",cex=0.4),
            "point" = returnGraph<- returnGraph+geom_point(colour="#04B431",size=0.4),
            "step"= returnGraph<- returnGraph+geom_step(colour="#04B431",direction = "hv"),
            "rug"=returnGraph<- returnGraph+geom_rug(colour="#04B431",sides = "bl")+geom_line(),
            "smooth"=returnGraph<- returnGraph+geom_smooth(colour="#04B431"),
            "text"=returnGraph<- returnGraph+geom_text(colour="#04B431",aes(label=file[,1]), nudge_x = 1, nudge_y = 1, check_overlap = T),
            "label"=returnGraph<- returnGraph+geom_label(colour="#04B431",aes(label=file[,1]), nudge_x = 1, nudge_y = 1, check_overlap = T),
            "jitter"=returnGraph<- returnGraph+geom_jitter(colour="#04B431",height = 2,width = 2)
    )
    #return graph
    returnGraph
  }
  
  else{
    #blank graph
    returnGraph <-ggplot()+geom_blank()
    returnGraph <- returnGraph + theme_bw()
    returnGraph <- returnGraph+labs(x=NULL, y=NULL)
    returnGraph <-returnGraph+theme(axis.title = element_text(face = "plain",hjust = 0.5, size=11))
    returnGraph <-returnGraph+ggtitle("Customized Graph")
    returnGraph <- returnGraph+theme(plot.title = element_text(family="serif", face = "bold",hjust = 0.5, size=18, color="darkblue"))
    returnGraph
  }
  
}
#customize graph of Individual version ~ y-axis is null
defaults <-function(inputData, inputXAxis, inputYAxis, graphType){
  
  file<-inputData
  
  if(length(which(!is.na(file)))!=0){
    #organize data with 1, file 
    ones<-c()
    ones[1:length(file)]<-1
    file<- data.frame(ones,file)
    #rename column
    colnames(file)<- c("inputXAxis","inputYAxis")
    
    #x-axis label of graph
    Intervaltemp = as.numeric(max(file[which(!is.na(file[,2])),2])-min(file[which(!is.na(file[,2])),2]))/8
    Interval<- seq(min(file[which(!is.na(file[,2])),2]),max(file[which(!is.na(file[,2])),2]),Intervaltemp)
    #for bin width 
    width <- as.numeric(max(file[which(!is.na(file[,2])),2])-min(file[which(!is.na(file[,2])),2]))/10
    
    #draw graph with one variable
    returnGraph <-ggplot(data = file, mapping=aes(x=file$inputYAxis))
    returnGraph <- returnGraph + theme_bw()
    returnGraph <- returnGraph+scale_x_continuous(breaks = Interval)
    returnGraph <- returnGraph+labs(x=inputXAxis)
    returnGraph <-returnGraph+theme(axis.title = element_text(face = "plain",hjust = 0.5, size=11))
    returnGraph <-returnGraph+ggtitle("Customized Graph")
    returnGraph <- returnGraph+theme(plot.title = element_text(family="serif", face = "bold",hjust = 0.5, size=18, color="darkblue"))
    #draw according to graphType
    switch (graphType,
            "area"= returnGraph<-returnGraph+geom_area(colour="#04B431",fill="#04B431",stat = "bin",binwidth=width),
            "density"=returnGraph<-returnGraph+geom_density(colour="#04B431",kernel="gaussian"),
            "dotplot"=returnGraph<-returnGraph+geom_dotplot(colour="#04B431",fill="#04B431",binwidth =width),
            "freqpoly"= returnGraph<-returnGraph+geom_freqpoly(colour="#04B431",fill="#04B431",binwidth=width),
            "histogram"=returnGraph<-returnGraph+geom_histogram(colour="#04B431",fill="#04B431",binwidth =width)
    )
    #return graph
    returnGraph
  }else{
    #blank graph
    returnGraph <-ggplot()+geom_blank()
    returnGraph <- returnGraph + theme_bw()
    returnGraph <- returnGraph+labs(x=NULL, y=NULL)
    returnGraph <-returnGraph+theme(axis.title = element_text(face = "plain",hjust = 0.5, size=11))
    returnGraph <-returnGraph+ggtitle("Customized Graph")
    returnGraph <- returnGraph+theme(plot.title = element_text(family="serif", face = "bold",hjust = 0.5, size=18, color="darkblue"))
    returnGraph
  }
  
}


