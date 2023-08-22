/* 只有从 screen://local/* 打开的网页才可使用该API

/* 执行指令 command=要执行的指令 */
function runCommand(command){
    window.ScreenInMC({
		request: JSON.stringify({
			"action": "run_command",
			"command": command
		})
	})
}
/* 输入红石信号需要更新红石中继器才能生效 */
/* 输出红石信号 id=接口ID value=红石信号(0-15) */
function outputRedstone(id,value){
    window.ScreenInMC({
		request: JSON.stringify({
			"action": "redstone_output",
			"id": id,
			"value": value
		})
	})
}
/* 启动红石信号监听 id=接口ID */
function listenRedstone(id){
    window.ScreenInMC({
        request: JSON.stringify({
            "action": "listen_redstone_input",
            "id": id
        })
    })
    window.addEventListener("message",function(e){
        if(e.data["type"]=="redstone_input"){
            console.log("输入接口 "+e.data["id"]+ " 收到强度为 "+e.data["value"]+" 的信号")
        }
    })
}
/* 停止红石信号监听 id=接口ID */
function stopListenRedstone(id){
    window.ScreenInMC({
        request: JSON.stringify({
            "action": "stop_listen_redstone_input",
            "id": id
        })
    })
}
