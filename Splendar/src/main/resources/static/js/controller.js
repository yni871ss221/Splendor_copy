app.controller("controller", function($scope, $filter, $sanitize) {

	var stompClient = null;
	
	$scope.message = "";
	$scope.messages = [];
	$scope.topicMessages = [];
	$scope.connected = false;
	$scope.required = true;
	$scope.topics = ['チャット部屋1', 'チャット部屋2', 'チャット部屋3'];
	$scope.topic = $scope.topics[0];
	$scope.gameStartButtonSwitch = false;
	// ゲーム中かどうかを判別するフラグ
	$scope.gamePlayingFlg = false;
	// トークン取得の方法を選択	
	$scope.token_mode = "get_another_token";

	$scope.players = new Object;

	$scope.my_player_info = new Player();
	$scope.gamePlayers = [];
	$scope.gameplayerOrder = [];
	
	/**
	 * Websocketのコネクションが張られているか
	 * @param {} connected 
	 */
	function setConnected(connected) {
		// 画面切り替えのためのフラグ
		$scope.connected = connected;
		// その他初期化
		$scope.messages = [];
		$scope.topicMessages = [];
		
		// データバインド更新
		$scope.$apply();
	}
	
	function showAlert(message) {
		$("#errorMessage").html(message);
		$("#errorPanel").modal({keyboard:false})
	}
	
	/**
	 * チャット開始押下時
	 */
	$scope.startChatting = () => {
		// ニックネームが入力されているか
		if($scope.my_player_info.username === null || $scope.my_player_info.username.trim() === "") {
			showAlert("Username is required.");
			return;
		}
		$scope.my_player_info.username = $scope.my_player_info.username.trim();

		// SockJSにより、エンドポイントのURL設定
		var socket = new SockJS('/splendor/spring-websocket-app');
		// WebSocketを使ったStompクライアントを作成
		stompClient = Stomp.over(socket);
		 // エンドポイントに接続し、接続した際のコールバックを登録
		stompClient.connect({}, onConnect, onError);
	};
	/**
	 * エンドポイントに接続した場合の処理
	 */
	function onConnect() {
		// コネクションをtrueへ更新
		setConnected(true);
		console.log('Connected');
		// 宛先が/topic/messagesのメッセージを取得し、コールバック処理を登録
		stompClient.subscribe('/topic/messages', receiveMessage);
		stompClient.subscribe('/topic/gamePlayerInfo', receiveGamePlayerInfo);
		stompClient.subscribe('/topic/leaveUserInfo', receiveLeavePlayer);
		
		// ゲーム開始ボタンが押下されサーバーから返却された際に呼び出されるメソッド
		stompClient.subscribe('/topic/setGameField', setGameField);

		// 宛先が/user/queue/errorsの場合は、エラーメッセージを表示
		stompClient.subscribe('/user/queue/errors', (error) => {
			showAlert(JSON.parse(error.body).message);
		});
		// 宛先/app/greetへJsonに格納してメッセージを送信
		stompClient.send("/app/adduser", {}, JSON.stringify({sender: $scope.my_player_info.username, type: 'JOIN', timestamp: new Date()}));
	}
	
	function onError(error) {
		console.log(error);
		showAlert("Could not connect to WebSocket server. Try after refresh this page.");
		setConnected(false);
	}
	
	$scope.disconnect = () => {
		if(stompClient !== null) {
			stompClient.disconnect();
		}
		setConnected(false);
		$scope.my_player_info.username = "";
		console.log("Disconnected");
	};
	
	$scope.sendMessage = () => {
		var message = $scope.message.trim();
		if(message && stompClient) {
			let chatMessage = {
				sender: $scope.my_player_info.username,
				content: $sanitize(message),
				type: 'CHAT',
				timestamp: new Date()
			};
			stompClient.send("/app/send_chat/" + $scope.topic, {}, JSON.stringify(chatMessage));
			$scope.message = "";
		}
	};

	/**
	 * 席に着くがクリックされた場合
	 * @param {*} chatMessage 
	 */
	$scope.sitTable = () => {

		// 席に着いたのでプレイヤーの情報を更新
		$scope.my_player_info.game_start_flg = true;

		if(stompClient){
			let gamePlayer = {
				name : $scope.my_player_info.username,
				avatar_url : $scope.my_player_info.avatar_url,
				mode : 'GAME_PLAY'
			};
			stompClient.send("/app/play_control", {}, JSON.stringify(gamePlayer));
		}
	};

	/**
	 * 席を離れるがクリックされた場合
	 */
	$scope.leaveTable = () => {

		// 席を離れたのでプレイヤーの情報を更新
		$scope.my_player_info.game_start_flg = false;

		if(stompClient){
			let gamePlayer = {
				name : $scope.my_player_info.username,
				avatar_url : $scope.my_player_info.avatar_url,
				mode : 'GAME_LEAVE'
			};
			stompClient.send("/app/play_control", {}, JSON.stringify(gamePlayer));
		}
	};

	/**
	 * ゲーム開始ボタンがクリックされた場合
	 */
	$scope.gameStart = () => {
		
		// 一番最初に着席しているプレイヤーのターンフラグを更新
		$scope.gamePlayers[0].value.turnFlg = true;
		$('#gamePlayer_info_' + $scope.gamePlayers[0].value.name).addClass('my_turn');

		if(stompClient){

			let player = {
				"userid": "user_id",
				"turnPlayerName": $scope.gamePlayers[0].value.name
			}
			stompClient.send("/app/game_start", {}, JSON.stringify(player));
		}
	};

	/**
	 * トークンが押された場合
	 * @param {*} chatMessage 
	 */
	$scope.tokenTilesClick = (e) =>{
		
		// プレイヤーの情報を取得
		var gamePlayerKey = getGamePlayerInfo($scope.gamePlayers, $scope.my_player_info.username);
		var player_info = $scope.gamePlayers[gamePlayerKey].value;

		// 自分のターンのみ
		if(player_info.turnFlg == true){
			var global = $scope.global;

			// 同じトークンを取得する場合
			if($scope.token_mode === "get_same_token"){
				// 最初に取得する場合は、取得したトークンのIDを保持
				if($scope.my_player_info.sameTokenID == ""){
					$scope.my_player_info.sameTokenID = e.key;
				// 2回目に取得する場合は、前回と同じものかを判別する
				}else if($scope.my_player_info.sameTokenID != e.key){
					alert("他のトークンは取得できません。");
					return false;
				}
			}

			var playerHasTotalToken = player_info.token.emerald +
				player_info.token.sapphire +
				player_info.token.ruby + 
				player_info.token.diamond +
				player_info.token.onyx +
				player_info.token.goldJoker;

			if($scope.token_mode === "get_another_token" && playerHasTotalToken + 3 > 10){				
				alert("トークンを10枚以上は保持できません。");
				return false;
			} else if($scope.token_mode === "get_same_token" && playerHasTotalToken + 2 > 10){
				alert("トークンを10枚以上は保持できません。");
				return false;
			}

			switch(e.key){
				case "emerald":
					if($scope.token_mode === "get_another_token" && $scope.my_player_info.token.emerald == 1){
						alert("エメラルドはすでに選択されています。");
					} else if($scope.token_mode === "get_same_token" && $scope.global.token.emerald - 2 < 2){
						alert("場に4枚以上トークンがなければ取得できません。");
					} else{
						if($scope.global.token.emerald == 0){
							alert("トークンがありません。");
						} else{
							if($scope.token_mode === "get_another_token"){
								$scope.my_player_info.token.emerald = $scope.my_player_info.token.emerald + 1;
								$scope.my_player_info.totalToken = $scope.my_player_info.totalToken + 1;
								$scope.global.token.emerald = $scope.global.token.emerald - 1;
							} else{
								$scope.my_player_info.token.emerald = $scope.my_player_info.token.emerald + 2;
								$scope.my_player_info.totalToken = $scope.my_player_info.totalToken + 2;
								$scope.global.token.emerald = $scope.global.token.emerald - 2;
							}
						}
					}
					break;
				case "sapphire" :
					if($scope.token_mode === "get_another_token" && $scope.my_player_info.token.sapphire == 1){
						alert("サファイアはすでに選択されています。");
					} else if($scope.token_mode === "get_same_token" && $scope.global.token.sapphire - 2 < 2){
						alert("場に4枚以上トークンがなければ取得できません。");
					} else{
						if($scope.global.token.sapphire == 0){
							alert("トークンがありません。");
						} else{
							if($scope.token_mode === "get_another_token"){
								$scope.my_player_info.token.sapphire = $scope.my_player_info.token.sapphire + 1;
								$scope.my_player_info.totalToken = $scope.my_player_info.totalToken + 1;
								$scope.global.token.sapphire = $scope.global.token.sapphire - 1;
							} else{
								$scope.my_player_info.token.sapphire = $scope.my_player_info.token.sapphire + 2;
								$scope.my_player_info.totalToken = $scope.my_player_info.totalToken + 2;
								$scope.global.token.sapphire = $scope.global.token.sapphire - 2;
							}
						}
					}
					break;
				case "ruby" :
					if($scope.token_mode === "get_another_token" && $scope.my_player_info.token.ruby == 1){
						alert("ルビーはすでに選択されています。");
					} else if($scope.token_mode === "get_same_token" && $scope.global.token.ruby - 2 < 2){
						alert("場に4枚以上トークンがなければ取得できません。");
					} else{
						if($scope.global.token.ruby == 0){
							alert("トークンがありません。");
						} else{
							if($scope.token_mode === "get_another_token"){
								$scope.my_player_info.token.ruby = $scope.my_player_info.token.ruby + 1;
								$scope.my_player_info.totalToken = $scope.my_player_info.totalToken + 1;
								$scope.global.token.ruby = $scope.global.token.ruby - 1;
							} else{
								$scope.my_player_info.token.ruby = $scope.my_player_info.token.ruby + 2;
								$scope.my_player_info.totalToken = $scope.my_player_info.totalToken + 2;
								$scope.global.token.ruby = $scope.global.token.ruby - 2;
							}
						}
					}
					break;
				case "diamond":
					if($scope.token_mode === "get_another_token" && $scope.my_player_info.token.diamond == 1){
						alert("ダイアモンドはすでに選択されています。");
					} else if($scope.token_mode === "get_same_token" && $scope.global.token.diamond - 2 < 2){
						alert("場に4枚以上トークンがなければ取得できません。");
					} else{
						if($scope.global.token.diamond == 0){
							alert("トークンがありません。");
						} else{
							if($scope.token_mode === "get_another_token"){
								$scope.my_player_info.token.diamond = $scope.my_player_info.token.diamond + 1;
								$scope.my_player_info.totalToken = $scope.my_player_info.totalToken + 1;
								$scope.global.token.diamond = $scope.global.token.diamond - 1;
							} else{
								$scope.my_player_info.token.diamond = $scope.my_player_info.token.diamond + 2;
								$scope.my_player_info.totalToken = $scope.my_player_info.totalToken + 2;
								$scope.global.token.diamond = $scope.global.token.diamond - 2;
							}
						}

					}
					break;
				case "onyx":
					if($scope.token_mode === "get_another_token" && $scope.my_player_info.token.onyx == 1){
						alert("オニキスはすでに選択されています。");
					}  else if($scope.token_mode === "get_same_token" && $scope.global.token.onyx - 2 < 2){
						alert("場に4枚以上トークンがなければ取得できません。");
					} else{
						if($scope.global.token.onyx == 0){
							alert("トークンがありません。");
						} else{
							if($scope.token_mode === "get_another_token"){
								$scope.my_player_info.token.onyx = $scope.my_player_info.token.onyx + 1;
								$scope.my_player_info.totalToken = $scope.my_player_info.totalToken + 1;
								$scope.global.token.onyx = $scope.global.token.onyx - 1;
							} else{
								$scope.my_player_info.token.onyx = $scope.my_player_info.token.onyx + 2;
								$scope.my_player_info.totalToken = $scope.my_player_info.totalToken + 2;
								$scope.global.token.onyx = $scope.global.token.onyx - 2;
							}
						}
					}
					break;
			}

			// データバインドを更新
			//$scope.$apply();

			if(($scope.token_mode === "get_another_token" && $scope.my_player_info.totalToken === 3)
				|| $scope.token_mode === "get_same_token" && $scope.my_player_info.totalToken === 2){
				
				if(window.confirm('トークンを取得しますか？')){

					var tokens = [];
					for(var key in $scope.my_player_info.token){
						if($scope.my_player_info.token[key] != 0){
							for(var i=0; i<$scope.my_player_info.token[key]; i++){
								tokens.push(key);
							}
						}
						// 初期化
						$scope.my_player_info.token[key] = 0;
					}
					
					// 初期化
					$scope.my_player_info.totalToken = 0;
					$scope.my_player_info.sameTokenID = "";

					var sendParam = {
						receiveJsonStr : JSON.stringify({
								"userid": $scope.my_player_info.username,
								"devCard1": {},
								"devCard2": {},
								"devCard3": {},
								"token": tokens,
								"nobleTiles": [],
								"event": "tokenTilesClick"
						})
					}
					
					stompClient.send("/app/game_event", {}, JSON.stringify(sendParam));

				}else{
					playCansel();
				}
				
			}
		}
	}

	/**
	 * 発展カードが押された場合
	 * @param {*} chatMessage 
	 */
	$scope.devCardLevel1Click = (e) =>{
		
		var devCard = new Object();
		devCard[String(e.id).toUpperCase()] = e.value;

		// 取得する対象の発展カードのIDから詳細なデータを取得
		var devCardData = $scope.gameData.devlopment_card_deck1_data[e.value];

		sendDevCardEvent(devCard, devCardData, 1);
	}

	$scope.devCardLevel2Click = (e) =>{
		
		var devCard = new Object();
		devCard[String(e.id).toUpperCase()] = e.value;

		// 取得する対象の発展カードのIDから詳細なデータを取得
		var devCardData = $scope.gameData.devlopment_card_deck2_data[e.value];

		sendDevCardEvent(devCard, devCardData, 2);
	}

	$scope.devCardLevel3Click = (e) =>{
		
		var devCard = new Object();
		devCard[String(e.id).toUpperCase()] = e.value;

		// 取得する対象の発展カードのIDから詳細なデータを取得
		var devCardData = $scope.gameData.devlopment_card_deck3_data[e.value];

		sendDevCardEvent(devCard, devCardData, 3);
	}
 
	/**
	 * メッセージが返却された場合の処理
	 * @param {} chatMessage 
	 */
	function receiveMessage(chatMessage) {
		var conversationArea = document.querySelector('#conversation');
		
		// 受け取り値をJsonへパース
		var message = JSON.parse(chatMessage.body);
		console.log(JSON.stringify(message[message.length - 1]));
		// これまで追加されたメッセージへ追加格納
		$scope.messages.push(message[message.length - 1]);

		// これまで追加されたログインユーザー分をループで回す
		for (var i=0; i<message.length; i++) {
			if(message[i].type === 'JOIN'){
				var player = new Player();
				player.name = message[i].sender;
				player.id = i + 1;
				player.avatar_url = player.avatar_url + player.id + ".png";
				$scope.players[player.name] = player;

				if(message[i].sender === $scope.my_player_info.username){
					$scope.my_player_info.avatar_url = player.avatar_url;
				}
			}
		}

		// トピックメッセージを作成
		makeTopicMessages();
		// データバインドを更新
		$scope.$apply();
		// 高さを調節
		conversationArea.scrollTop = conversationArea.scrollHeight;
	} 
	
	/**
	 * 持っている発展カードを閲覧する
	 */
	$scope.browseDevCard = function(e, player){
		var value = e.value;
		var htmlCode = "<table><tr>";
		var gameData = new Object();
		for(var i=0; i<value.length; i++){
			if(value[i].level == 1){
				gameData = $scope.gameData.devlopment_card_deck1_data;
			} else if(value[i].level == 2){
				gameData = $scope.gameData.devlopment_card_deck2_data;
			} else{
				gameData = $scope.gameData.devlopment_card_deck3_data;
			}
			htmlCode +=	"<td align='center'>";

			if(e.key=="goldJoker"){
				htmlCode +=	"<input type='radio' id='keepDevCard_" + i + "' name='keepDevCardBrowser' value='" + value[i].id + "' data-level='"+ value[i].level +"' data-player='"+ player +"' " + key + "'>"+
				"<label class='devCard " + gameData[value[i].id].color +" ' id="+ value[i].id +"' for='keepDevCard_" + i + "'>"+
					"<span>" +
						"<div class='devCard_score'>"+
							gameData[value[i].id].score +
						"</div>" + 
						"<div class='devCard_costs'>";

						if(gameData[value[i].id].cost.emerald != 0){
							htmlCode += "<div class='dev_cost emerald'>" + gameData[value[i].id].cost.emerald + "</div>";
						}
						if(gameData[value[i].id].cost.sapphire != 0){
							htmlCode += "<div class='dev_cost sapphire'>" + gameData[value[i].id].cost.sapphire + "</div>";
						}
						if(gameData[value[i].id].cost.ruby != 0){
							htmlCode += "<div class='dev_cost ruby'>" + gameData[value[i].id].cost.ruby + "</div>";
						}
						if(gameData[value[i].id].cost.diamond != 0){
							htmlCode += "<div class='dev_cost diamond'>" + gameData[value[i].id].cost.diamond + "</div>";
						}
						if(gameData[value[i].id].cost.onyx != 0){
							htmlCode += "<div class='dev_cost onyx'>" + gameData[value[i].id].cost.onyx + "</div>";
						}
						htmlCode += "</div>" +			
					"</span>"+
				"</label>";
			} else{
				htmlCode +=	"<div class='devCard " + gameData[value[i].id].color +" ' id="+ value[i].id +"'>" +
					"<span>" +
						"<div class='devCard_score'>"+
							gameData[value[i].id].score +
						"</div>" + 
						"<div class='devCard_costs'>";

						if(gameData[value[i].id].cost.emerald != 0){
							htmlCode += "<div class='dev_cost emerald'>" + gameData[value[i].id].cost.emerald + "</div>";
						}
						if(gameData[value[i].id].cost.sapphire != 0){
							htmlCode += "<div class='dev_cost sapphire'>" + gameData[value[i].id].cost.sapphire + "</div>";
						}
						if(gameData[value[i].id].cost.ruby != 0){
							htmlCode += "<div class='dev_cost ruby'>" + gameData[value[i].id].cost.ruby + "</div>";
						}
						if(gameData[value[i].id].cost.diamond != 0){
							htmlCode += "<div class='dev_cost diamond'>" + gameData[value[i].id].cost.diamond + "</div>";
						}
						if(gameData[value[i].id].cost.onyx != 0){
							htmlCode += "<div class='dev_cost onyx'>" + gameData[value[i].id].cost.onyx + "</div>";
						}
						htmlCode += "</div>" +			
					"</span>"+
				"</div>";
			}
			htmlCode +=	 "</td>";
		}

		htmlCode += "</tr></table>";

		$('#sentence').html(htmlCode);
		// ダイアログの呼び出し
		$("#browserDevCardDialog").dialog("open");
	}

	/**
	 * メッセージの種類(入室・退室・メッセージ)によって、クラスの切り分けを行う
	 */
	$scope.getChatClass = function(index, messages) {
		// トピックメッセージを取得
		let mes = messages[index];
		if(mes.type === 'CHAT') {
			return "chat-message";
		} else {
			return "event-message";
		}
	};
	
	/**
	 * チャット部屋が切り替わった際の処理
	 */
	$scope.topicChanged = function() {
		makeTopicMessages();
	};
	
	/**
	 * アクティブなチャット部屋のメッセージを取得
	 */
	function makeTopicMessages() {
		$scope.topicMessages = $scope.messages.filter(mes => mes.topic === $scope.topic || mes.topic === null);
	}

	/**
	 * 席につく・離籍ボタンがクリックされた際の処理
	 * @param {*} gamePlayers 
	 */
	function receiveGamePlayerInfo(gamePlayers){

		// 受け取り値をJsonへパース
		var gamePlayer = JSON.parse(gamePlayers.body);
		console.log(JSON.stringify(gamePlayer));
		
		if(gamePlayer.mode === 'GAME_PLAY'){
			var player = new Player();
			var dict = new Object();

				player.name = gamePlayer.name;
				player.userid = gamePlayer.userid;
				player.avatar_url = gamePlayer.avatar_url;

        		dict["no"] = $scope.gamePlayers.length + 1;
        		dict["id"] = gamePlayer.name;
				dict["value"] = player;

				$scope.gamePlayers.push(dict);

		} else if(gamePlayer.mode === 'GAME_LEAVE'){

			// 該当するプレイヤーを削除
			$scope.gamePlayers.some(function(v, i){
				if (v.value.name==gamePlayer.name) $scope.gamePlayers.splice(i,1);
			});
		}

		/**
		 * ゲーム人数が2人以上であればゲーム開始ボタンを押せるようにする(席についているプレイヤーのみ)
		 */
		if($scope.my_player_info.game_start_flg && $scope.gamePlayers.length >= 2){
			$scope.gameStartButtonSwitch = true;
		} else{
			$scope.gameStartButtonSwitch = false;
		}

		// データバインドを更新
		$scope.$apply();
	}
	
	/**
	 * 画面からログアウトした際の処理
	 * @param {*} leavePlayer 
	 */
	function receiveLeavePlayer(leavePlayer){

		// 受け取り値をJsonへパース
		var leavePlayer = JSON.parse(leavePlayer.body);
		console.log(JSON.stringify(leavePlayer));

		// ゲーム上からログアウトした利用者を削除
		$scope.gamePlayers.some(function(v, i){
			if (v.value.name==leavePlayer.sender) $scope.gamePlayers.splice(i,1);
		});

		delete $scope.players[leavePlayer.sender];

		var message = new Object;
		message["sender"] = leavePlayer.sender;
		message["topic"] = null;
		message["type"] = "LEAVE";
		message["timestamp"] = new Date();
		
		$scope.messages.push(message);
		makeTopicMessages();
		
		// データバインドを更新
		$scope.$apply();
	}
	
	/**
	 * フィールド上の発展カードを取得するためのイベント
	 * @param {*} devCard・・・取得対象の発展カード({場所：ID})
	 * @param {*} devCardData ・・・取得対象の発展カードの詳細データ
	 * @param {*} devKindFlg ・・・発展カードのレベルフラグ
	 */
	function sendDevCardEvent(devCard, devCardData, devKindFlg){
		
		// プレイヤーの情報を取得
		var gamePlayerKey = getGamePlayerInfo($scope.gamePlayers, $scope.my_player_info.username);
		var player_info = $scope.gamePlayers[gamePlayerKey].value;

		// 自分のターンのみ
		if(player_info.turnFlg == true){
			for (var key in devCard) {
				// 押された発展カードのIDが-1なら何もないのでフラグを更新
				if(devCard[key] == -1){
					return false;
				}
			}

			var token = [];
			var validate_flg = false;
			var devCard1 = {};
			var devCard2 = {};
			var devCard3 = {};

			// 初期化
			$scope.my_player_info.usedGoleJoker = 0;
			$scope.my_player_info.selectedDataCard = new Object();
			$scope.my_player_info.selectedGoldJokerToken = new TokenJewel();

			// 現在処理している発展カードの種類によって返却する値を設定
			if(devKindFlg === 1){
				devCard1 = devCard;
			} else if(devKindFlg === 2){
				devCard2 = devCard;
			} else{
				devCard3 = devCard;
			}
			// 発展カードを設定
			$scope.my_player_info.selectedDevCards = new Array(devCard1, devCard2, devCard3);

			// モードが発展カードのキープの場合
			if($scope.token_mode === "keep_dev_card"){
					
				token.push("goldJoker");
				$scope.my_player_info.eventMode = "keep";
				
				if($scope.global.token.goldJoker == 0){
					alert("ゴールドジョーカーがありません。");
					return false;
				}
				// キープされているカードが3枚の場合はエラー
				if(player_info.devCard.goldJoker.length == 3){
					alert("キープは3枚までです。");
					return false;
				}
			} else{
				// 取得対象のコストを取得
				var cost = devCardData.cost;
				// ゴールドジョーカーを使用するかどうかを管理するフラグ
				var goldJokerFlg = false;
				$scope.my_player_info.eventMode = "depCardDeckClick";
				
				if(player_info.token.goldJoker > 0){
					if(window.confirm('ゴールドジョーカーを使用しますか？')){


						// 使ったゴールドジョーカーの数をカウント
						$scope.my_player_info.usedGoleJoker = $scope.my_player_info.usedGoleJoker + 1;
						$scope.my_player_info.selectedDataCard = devCardData;
							
						createGoldJokerDialog(cost);
						goldJokerFlg = true;
				
						// ダイアログの呼び出し
						$("#goldJokerDialog").dialog("open");
					}
				}
				// ゴールドジョーカー以外の場合
				if(!goldJokerFlg){
					validate_flg = validateAndSetDevCard(cost, token);
				}
			}

			// エラー等がない場合はサーバーへ
			if(!validate_flg && !goldJokerFlg){
				sendStompClientOfGameEvent(
					devCard1, 
					devCard2,
					devCard3,
					token,
					[],
					$scope.my_player_info.eventMode);
			}
		}
	}

	/**
	 * ゲームフィールドの設定
	 */
	function setGameField(gameForm){
		
		// 受け取り値をJsonへパース
		var form = JSON.parse(gameForm.body);

		// ゲームフィールドの初期化時の場合のみ、発展カード、貴族カードの詳細データを取得する
		if(form.event === EVENT_CODE_GAME_INIT){
			
			$scope.gamePlayingFlg = true;
			$scope.gameData = form.gameData;

			// 最初のプレイヤーのターンを更新する
			var gamePlayerKey = getGamePlayerInfo($scope.gamePlayers, form.turnPlayerName);
			$scope.gamePlayers[gamePlayerKey].value.turnFlg = true;
			$('#gamePlayer_info_' + $scope.gamePlayers[gamePlayerKey].value.name).addClass('my_turn');
			
		}

		$scope.game_start_flg = true;
		$scope.global = form.global;
		var player = form.player;
		
		if(player.name !== ""){
			var gamePlayerKey = getGamePlayerInfo($scope.gamePlayers, player.name);
			objectToClass(player.devCard, $scope.gamePlayers[gamePlayerKey].value.devCard);
			objectToClass(player.token, $scope.gamePlayers[gamePlayerKey].value.token);
			$scope.gamePlayers[gamePlayerKey].value.nobleTiles = player.nobleTiles;
			$scope.gamePlayers[gamePlayerKey].value.score = player.score;

			// ターンが終了したので次の人の番へ以降する
			if(form.event !== EVENT_CODE_NOBLE){
				
				// トークン取得の方法を選択を初期値へ
				$scope.token_mode = "get_another_token";
				
				for(var i=0; i<$scope.gamePlayers.length ; i++){
					if($scope.gamePlayers[i].value.turnFlg == true){
						// ターンフラグを元に戻す
						$scope.gamePlayers[i].value.turnFlg = false;
						$('#gamePlayer_info_' + $scope.gamePlayers[i].value.name).removeClass('my_turn');

						// 次の人のターンフラグを更新する(最後の番の人の場合は、1番目の人のフラグを更新)
						if(i == $scope.gamePlayers.length -1){
							$scope.gamePlayers[0].value.turnFlg = true;
							$('#gamePlayer_info_' + $scope.gamePlayers[0].value.name).addClass('my_turn');
						} else{
							$scope.gamePlayers[i+1].value.turnFlg = true;
							$('#gamePlayer_info_' + $scope.gamePlayers[i+1].value.name).addClass('my_turn');
						}
						break;
					}
				}
			}
		}

		$scope.global.nobleTiles = changeDictToArray(form.global.nobleTiles);
		$scope.global.devCard.level1 = changeDictToArray(form.global.devCard.level1);
		$scope.global.devCard.level2 = changeDictToArray(form.global.devCard.level2);
		$scope.global.devCard.level3 = changeDictToArray(form.global.devCard.level3);
		//$scope.global.token = changeDictToArray(form.global.token);

		// データバインドを更新
		$scope.$apply();

		// 貴族カードが取得できるかをチェック
		if(player.name == $scope.my_player_info.username){
			nobleGetCheck(player);
		}

		// 得点が15点を越えていたらゲーム終了
		if(player.score >= 15){
			alert(player.name + "さんが優勝しました。");
		}
	}
	
	/**
	 * トークンの取得モードが変更された場合
	 */
	$scope.tokenModeChange = function() {
		playCansel();
	};
	function playCansel(){

		// キャンセルされた場合は、取得した場のトークンを元にもどす
		for(var key in $scope.my_player_info.token){
			switch(key){
				case "emerald":
					if($scope.my_player_info.token.emerald != 0){
						$scope.global.token.emerald = $scope.global.token.emerald + $scope.my_player_info.token.emerald;
						$scope.my_player_info.token.emerald = 0;
					}
					break;
				case "sapphire" :
					if($scope.my_player_info.token.sapphire != 0){
						$scope.global.token.sapphire = $scope.global.token.sapphire + $scope.my_player_info.token.sapphire;
						$scope.my_player_info.token.sapphire = 0;
					}
					break;
				case "ruby" :
					if($scope.my_player_info.token.ruby != 0){
						$scope.global.token.ruby = $scope.global.token.ruby + $scope.my_player_info.token.ruby;
						$scope.my_player_info.token.ruby = 0;
					}
					break;
				case "diamond":
					if($scope.my_player_info.token.diamond != 0){
						$scope.global.token.diamond = $scope.global.token.diamond + $scope.my_player_info.token.diamond;
						$scope.my_player_info.token.diamond = 0;
					}
					break;
				case "onyx":
					if($scope.my_player_info.token.onyx != 0){
						$scope.global.token.onyx = $scope.global.token.onyx + $scope.my_player_info.token.onyx;
						$scope.my_player_info.token.onyx = 0;
					}
					break;
			}
		}

		// 初期化
		$scope.my_player_info.totalToken = 0;
		$scope.my_player_info.sameTokenID = "";
	}

	/**
	 * 貴族カードが取得できるかをチェックする
	 */
	function nobleGetCheck(player){
		var get_noble_tiles = [];
		for(var i=0; i<$scope.global.nobleTiles.length; i ++){
			var noble_get_flg = false;

			if($scope.global.nobleTiles[i].value != -1){
				var noble_cost = $scope.gameData.noble_card_data[$scope.global.nobleTiles[i].value].cost;
				for(key in player.devCard){
					// コストが0以外で、判定
					if(noble_cost[key] != 0){
						// 1つでも基準を満たしていない場合はfalseにして処理を終了
						if(player.devCard[key].length >= noble_cost[key]){
							noble_get_flg = true;
						} else{
							noble_get_flg = false;
							break;
						}
					}
				}
			}

			// 基準を満たしている場合は貴族カードのidを格納
			if(noble_get_flg){
				get_noble_tiles.push($scope.global.nobleTiles[i].value);
				// 点数を加算
				var gamePlayerKey = getGamePlayerInfo($scope.gamePlayers, player.name)
				$scope.gamePlayers[gamePlayerKey].value.score = $scope.gamePlayers[gamePlayerKey].value.score + $scope.gameData.noble_card_data[$scope.global.nobleTiles[i].value].score;
			}
		}

		if(get_noble_tiles.length != 0){
			var sendParam = {
				receiveJsonStr : JSON.stringify({
						"userid": $scope.my_player_info.username,
						"devCard1": {},
						"devCard2": {},
						"devCard3": {},
						"token": [],
						"nobleTiles": get_noble_tiles,
						"event": "nobleTiles"
				})
			}
			stompClient.send("/app/game_event", {}, JSON.stringify(sendParam));
		}
	}

	/**
	 * ゴールドジョーカーの使用対象を決めるダイアログの作成
	 * @param {*} cost 
	 */
	function createGoldJokerDialog(cost){
		var innerHtmlCode = $scope.my_player_info.usedGoleJoker + "枚目のゴールドジョーカーを使用する対象を選択してください。<br />";
		var tokenCounter = 0;
		for (var key in cost) {
			if(cost[key] != 0){
				innerHtmlCode += "<input type='radio' id='goldjoker_" + tokenCounter + "' name='goldjoker' value='" + key + "'><label class='player_devCard " + key + "' for='goldjoker_" + tokenCounter + "'></label>";
				tokenCounter++;
			}
		}
		document.getElementById('selecter').innerHTML = innerHtmlCode;
	}

	/**
	 * 発展カード取得時のチェック(トークンの数など)を行い、大丈夫であれば支払うコストを詰めた配列を返却する
	 * @param {*} cost 
	 */
	function validateAndSetDevCard(cost, token){

		var errorFlg = false;

		// プレイヤーの情報を取得
		var gamePlayerKey = getGamePlayerInfo($scope.gamePlayers, $scope.my_player_info.username)
		var player_info = $scope.gamePlayers[gamePlayerKey].value;

		// 取得する発展カードに対してトークンの数が見合っているかどうかvalidateする  
		for (var key in cost) {
			if(cost[key] != 0){
				// プレイヤーが持っている発展カードとトークンの合計が取得する発展カードのトークンよりも超えている場合は取得可能
				if((player_info.token[key] + player_info.devCard[key].length + $scope.my_player_info.selectedGoldJokerToken[key]) >= cost[key]){
					// 取得する発展カードのトークンからプレイヤーが取得している発展カードの枚数を引いて足りない分のコストをトークンから払う。
					for(var i=0; i < (cost[key] - player_info.devCard[key].length - $scope.my_player_info.selectedGoldJokerToken[key]); i++){
						token.push(key);
					}
					// ゴールドジョーカーの分をカウント
					for(i=0; i<$scope.my_player_info.selectedGoldJokerToken[key]; i++){
						token.push("goldJoker");
					}
				} else{
					var error = cost[key] - (player_info.token[key] + player_info.devCard[key].length + $scope.my_player_info.selectedGoldJokerToken[key]);
					alert(key + "トークンが" + error + "枚、足りません。");
					errorFlg = true;
				}
			}
		}
		return errorFlg;
	}

	function exchangeKeepCard(e){
		alert("click");
	}

	function sendStompClientOfGameEvent(devCard1, devCard2, devCard3, token, nobleTiles, event){
		var sendParam = {
			receiveJsonStr : JSON.stringify({
					"userid": $scope.my_player_info.username,
					"devCard1": devCard1,
					"devCard2": devCard2,
					"devCard3": devCard3,
					"token": token,
					"nobleTiles": nobleTiles,
					"event": event
			})
		}
		stompClient.send("/app/game_event", {}, JSON.stringify(sendParam));
	}

	// ダイアログの初期設定
	$("#goldJokerDialog").dialog({
		autoOpen: false,  // 自動的に開かないように設定
		width: '100%',       // 横幅のサイズを設定
		maxWidth: 500,
		modal: true,      // モーダルダイアログにする
		buttons: [        // ボタン名 : 処理 を設定
		{
			text: 'OK',
			click: function(){
				
				var token = [];
				var validate_flg  = true;
				
				// 選択されたトークンを取得
				var select_token = $("input[name=goldjoker]:checked").val();
				// トークンの数を一時的に保管
				$scope.my_player_info.selectedGoldJokerToken[select_token] ++;

				var cost = $scope.my_player_info.selectedDataCard.cost;
				// プレイヤーの情報を取得
				var gamePlayerKey = getGamePlayerInfo($scope.gamePlayers, $scope.my_player_info.username)
				var player_info = $scope.gamePlayers[gamePlayerKey].value;

				// 使用するゴールドジョーカと保持しているカードが一致しているなら次の処理
				if($scope.my_player_info.usedGoleJoker == player_info.token.goldJoker){

					validate_flg = validateAndSetDevCard(cost, token);

					if(!validate_flg){
						sendStompClientOfGameEvent($scope.my_player_info.selectedDevCards[0], 
							$scope.my_player_info.selectedDevCards[1],
							$scope.my_player_info.selectedDevCards[2],
							token,
							[],
							$scope.my_player_info.eventMode);
					}

					$(this).dialog('close');
				} else{
					// 他にもゴールドジョーカを使用するかダイアログ
					if(window.confirm('次のゴールドジョーカーも使用しますか？')){
						// 使ったゴールドジョーカーの数をカウント
						$scope.my_player_info.usedGoleJoker = $scope.my_player_info.usedGoleJoker + 1;
						createGoldJokerDialog(cost);
					} else{
						validate_flg = validateAndSetDevCard(cost, token);

						if(!validate_flg){
							sendStompClientOfGameEvent($scope.my_player_info.selectedDevCards[0], 
								$scope.my_player_info.selectedDevCards[1],
								$scope.my_player_info.selectedDevCards[2],
								token,
								[],
								$scope.my_player_info.eventMode);
						}
	
						$(this).dialog('close');
					}
				}
			}
		},
		{
			text: 'キャンセル',
			click: function(){
				$(this).dialog('close');
			}
		}
		]
	});

	// ダイアログの初期設定
	$("#browserDevCardDialog").dialog({
		autoOpen: false,  // 自動的に開かないように設定
		width: '100%',       // 横幅のサイズを設定
		maxWidth: 500,
		modal: true,      // モーダルダイアログにする
		buttons: [        // ボタン名 : 処理 を設定
		{
			text: 'OK',
			click: function(){

				
				// 選択されたトークンを取得
				var selected_keep_dev_card = $("input[name=keepDevCardBrowser]:checked").val();
				// プレイヤーの情報を取得
				var gamePlayerKey = getGamePlayerInfo($scope.gamePlayers, $scope.my_player_info.username);
				var player_info = $scope.gamePlayers[gamePlayerKey].value;

				// キープされている発展カードかつ自分のキープしている発展カードが選択されて自分のターンの場合は、キープされている発展カードの交換イベントを開始する
				if(selected_keep_dev_card != void 0 && $scope.my_player_info.username == $("input[name=keepDevCardBrowser]:checked")[0].dataset.player && player_info.turnFlg == true){

					// プレイヤーの情報を取得
					var gamePlayerKey = getGamePlayerInfo($scope.gamePlayers, $scope.my_player_info.username)
					var player_info = $scope.gamePlayers[gamePlayerKey].value;
					var token = [];
					var validate_flg = false;
					var selected_id = selected_keep_dev_card;
					var selected_level = $("input[name=keepDevCardBrowser]:checked")[0].dataset.level;
					var devCard1 = {};
					var devCard2 = {};
					var devCard3 = {};

					// 初期化
					$scope.my_player_info.usedGoleJoker = 0;
					$scope.my_player_info.selectedDataCard = new Object();
					$scope.my_player_info.selectedGoldJokerToken = new TokenJewel();

					var gameData = new Object();
					if(selected_level == 1){
						gameData = $scope.gameData.devlopment_card_deck1_data;
						devCard1["OTHER"] = selected_id;
					} else if(selected_level == 2){
						gameData = $scope.gameData.devlopment_card_deck2_data;
						devCard2["OTHER"] = selected_id;
					} else{
						gameData = $scope.gameData.devlopment_card_deck3_data;
						devCard3["OTHER"] = selected_id;
					}
					var devCardData = gameData[selected_id];
					// 発展カードを設定
					$scope.my_player_info.selectedDevCards = new Array(devCard1, devCard2, devCard3);
					// イベントを設定
					$scope.my_player_info.eventMode = "exchangeKeepDevCard";

					// 取得対象のコストを取得
					var cost = devCardData.cost;
					// ゴールドジョーカーを使用するかどうかを管理するフラグ
					var goldJokerFlg = false;
					
					if(player_info.token.goldJoker > 0){
						if(window.confirm('ゴールドジョーカーを使用しますか？')){

							// 使ったゴールドジョーカーの数をカウント
							$scope.my_player_info.usedGoleJoker = $scope.my_player_info.usedGoleJoker + 1;
							$scope.my_player_info.selectedDataCard = devCardData;
								
							createGoldJokerDialog(cost);
							goldJokerFlg = true;
					
							// ダイアログの呼び出し
							$("#goldJokerDialog").dialog("open");
						}
					}
					// ゴールドジョーカーを使用する場合は独自の処理を行うためその後の処理をパス
					if(!goldJokerFlg){
						validate_flg = validateAndSetDevCard(cost, token);
					}

					if(!validate_flg && !goldJokerFlg){
						sendStompClientOfGameEvent(
							devCard1, 
							devCard2,
							devCard3,
							token,
							[],
							$scope.my_player_info.eventMode);
					}
			
				}
				$(this).dialog('close');
			}
		}
		]
	});
});

function exchangeKeepCard(e){
	alert("click");
}

