class Player {
    
    constructor(){
        this._name = '';
        this._userid = 0;
        this._avatar_url = '/Splendar_copy/img/avatar/player';
        this._game_start_flg = false;
        this._score = 0;
        this._devCard = new DevCardJewel();
        this._token = new TokenJewel();
        this._nobleTiles = [];
        this._totalToken = 0;
        // 現在自分のターンかどうかを判別するフラグ
        this._turnFlg = false;
        // 同じトークンを2つ取得時にどのトークンが取得されたのかを一時的に保管しておく
        this._sameTokenID = "";
        //　ゴールドジョーカーを処理した枚数を一時的に保管しておく
        this._usedGoleJoker = 0;
        // 選択された発展カードのデータを一時的に保管しておく
        this._selectedDataCard = new Object();
        // 使用するゴールドジョーカーのトークンを一時的に保管しておく
        this._selectedGoldJokerToken = new TokenJewel();
        // 取得対象の発展カード({場所：ID})
        this._selectedDevCards = [];
        // 現在のイベントを保管しておく
        this._eventMode = "";
    }

    get id(){
        return this._id;
    }
    set id(id){
        this._id = id;
    }

    get name(){
        return this._name;
    }
    set name(name){
        this._name = name;
    }

    get avatar_url(){
        return this._avatar_url;
    }
    set avatar_url(avatar_url){
        this._avatar_url = avatar_url;
    }

    get game_start_flg(){
        return this._game_start_flg;
    }
    set game_start_flg(flg){
        this._game_start_flg = flg;
    }

    get score(){
        return this._score;
    }
    set score(score){
        this._score = score;
    }

    get devCard(){
        return this._devCard;
    }

    get token(){
        return this._token;
    }

    get nobleTiles(){
        return this._nobleTiles;
    }
    set nobleTiles(nobleTiles){
        this._nobleTiles = nobleTiles;
    }

    get totalToken(){
        return this._totalToken;
    }
    set totalToken(totalToken){
        this._totalToken = totalToken;
    }

    get turnFlg(){
        return this._turnFlg;
    }
    set turnFlg(turnFlg){
        this._turnFlg = turnFlg;
    }

    get sameTokenID(){
        return this._sameTokenID;
    }
    set sameTokenID(sameTokenID){
        this._sameTokenID = sameTokenID;
    }

    get usedGoleJoker(){
        return this._usedGoleJoker;
    }
    set usedGoleJoker(usedGoleJoker){
        this._usedGoleJoker = usedGoleJoker;
    }

    get selectedDataCard(){
        return this._selectedDataCard;
    }
    set selectedDataCard(selectedDataCard){
        this._selectedDataCard = selectedDataCard;
    }

    get selectedGoldJokerToken(){
        return this._selectedGoldJokerToken;
    }
    set selectedGoldJokerToken(selectedGoldJokerToken){
        this._selectedGoldJokerToken = selectedGoldJokerToken;
    }

    get selectedDevCards(){
        return this._selectedDevCards;
    }
    set selectedDevCards(selectedDevCards){
        this._selectedDevCards = selectedDevCards;
    }

    get eventMode(){
        return this._eventMode;
    }
    set eventMode(eventMode){
        this._eventMode = eventMode;
    }
    
}