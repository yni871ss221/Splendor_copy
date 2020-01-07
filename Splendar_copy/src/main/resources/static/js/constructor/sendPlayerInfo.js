class SendPlayerInfo {
    
    constructor(){
        this.userid = '';
        this.devCard = [];
        this.token = [];
        this.nobleTiles = [];
    }

    get m_userid(){
        return this.userid;
    }
    set m_userid(userid){
        this.userid = userid;
    }

    get m_devCard(){
        return this.devCard;
    }
    set m_devCard(devCard){
        this.devCard = devCard;
    }

    get m_token(){
        return this.token;
    }
    set m_token(token){
        this.token = token;
    }

    get m_nobleTiles(){
        return this.nobleTiles;
    }
    set m_nobleTiles(nobleTiles){
        this.nobleTiles = nobleTiles;
    }
}