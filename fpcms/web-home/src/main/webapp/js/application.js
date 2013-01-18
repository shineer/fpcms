function disableSubmit(finalResult,submitButtonId) {
	if(finalResult) {
		document.getElementById(submitButtonId).disabled = true;
		return finalResult;
	}else {
		return finalResult;
	}
}

function batchDelete(action,checkboxName,form){
    if (!hasOneChecked(checkboxName)){
            alert('请选择要操作的对象!');
            return;
    }
    if (confirm('确定执行[删除]操作?')){
        form.action = action;
        form.submit();
    }
}

function hasOneChecked(name){
    var items = document.getElementsByName(name);
    if (items.length > 0) {
        for (var i = 0; i < items.length; i++){
            if (items[i].checked == true){
                return true;
            }
        }
    } else {
        if (items.checked == true) {
            return true;
        }
    }
    return false;
}

function setAllCheckboxState(name,state) {
	var elms = document.getElementsByName(name);
	for(var i = 0; i < elms.length; i++) {
		elms[i].checked = state;
	}
}

function getReferenceForm(elm) {
	while(elm && elm.tagName != 'BODY') {
		if(elm.tagName == 'FORM') return elm;
		elm = elm.parentNode;
	}
	return null;
}

/**
 * 添加URL进网页收藏夹
 * @param title
 */
function addFavorite(title) {
   var url = location.protocol + "//" +location.host;
   if (document.all)
   {
      window.external.addFavorite(url,title);
   }
   else if (window.sidebar)
   {
      window.sidebar.addPanel(title, url, "");
   }
}


/**
 * 保护联系方式的js
 * 
 */
function tmplWithSiteProperty(stringTemplate, servletContentPath,site) {
	var request = $.ajax({url:servletContentPath+"/rpc/PropertyWebService/getSiteProperty?site="+site,dataType : "json"});
	request.done(function(response) { 
		var data = response.result;
		var resultString = tmpl(stringTemplate,data);
		
		$("#"+stringTemplate).empty();
		$("#"+stringTemplate).append(resultString);
		$("#"+stringTemplate).show();
		//callbackFunc(resultString);
	});
}