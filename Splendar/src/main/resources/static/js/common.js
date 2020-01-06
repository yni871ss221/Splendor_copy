function objectToClass(obj, cls){

    for(key in obj){
        cls[key] = obj[key];
    }
    return cls;
}

function changeTokenOrder(token){
    var retArray = [];

    for(key in token_order){
        var retDict = {};
        retDict["id"] = token_order[key];
        retDict["value"] = token[token_order[key]];

        retArray.push(retDict);
    }
    return retArray;
}

function keyChangeOfGetSetMethod(dict){
    var retDict = {};
    for(key in dict){
        retDict[key.slice(1)] = dict[key];
    }
    return retDict;
}

/**
 * フィールド上に設置する際にMapのままだと順番を変更できないため配列に入れ替える
 * @param {*} dict 
 */
function changeDictToArray(dict){
    
    var retArray = [];
    var count=0;
    var token_goldJoker = {};

    for(key in dict){
        var retDict = {};
        retDict["no"] = count;
        retDict["id"] = key;
        retDict["value"] = dict[key];

        // トークンのゴールドは一番最後に入れ替えるため保持
        if(key == "goldJoker"){
            token_goldJoker = retDict;
        } else{
            count ++;
            retArray.push(retDict);
        }
    }

    if(token_goldJoker.no !== void 0){
        token_goldJoker.no = retArray.length + 1;
        retArray.push(token_goldJoker);
    }
    return retArray;
}

/**
 * キー(プレイヤー名)からゲームプレイヤー配列の中の何番目の要素化を返却する
 * @param {} gamePlayers 
 * @param {*} key 
 */
function getGamePlayerInfo(gamePlayers, key){
    for(var i=0; i<gamePlayers.length; i++){
        if(gamePlayers[i].value.name == key){
            return i;
        }
    }
}