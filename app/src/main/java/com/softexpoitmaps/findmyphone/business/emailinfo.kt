package com.softexpoitmaps.findmyphone.business

class emailinfo {


    private var locateemail =""
    private var locatepin =""
    private var nickname = ""
    private var locateuid=""
private var checked=""

    constructor()
    constructor(locateemail: String, locatepin: String, nickname: String,locateuid:String,checked:String) {
        this.locateemail = locateemail
        this.locatepin = locatepin
        this.nickname = nickname
        this.checked=checked
        this.locateuid=locateuid
    }
    fun getlocatepin():String?{
        return locatepin
    }
    fun setlocatepin(locatepin:String){
        this.locatepin=locatepin
    }



    fun getlocateemail():String?{
        return locateemail
    }
    fun setlocateemail(locateemail:String){
        this.locateemail=locateemail
    }

    fun getnickname():String?{
        return nickname
    }
    fun setnickname(nickname:String){
        this.nickname=nickname
    }




    fun getchecked():String?{
        return checked
    }
    fun setchecked(checked:String){
        this.checked=checked
    }


    fun getlocateuid():String?{
        return locateuid
    }
    fun setlocateuid(locateuid:String){
        this.locateuid=locateuid
    }


}