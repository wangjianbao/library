[#ftl]
[@b.head/]
<table width="100%">
	<tr>
	  <td class="infoTitle" width="20%" style="height:22px;">
	   <img src="${base}/static/images/action/info.gif" align="top"/><em>
		  <em>我的照片</em>
	  </td>
	  <td class="infoTitle" width="20%" style="height:22px;">
 		<font color="red">&nbsp;[@b.messages/]</font>
	  </td>
	</tr>
	<tr>
	  <td colspan="5" style="font-size:0px">
		  <img src="${base}/static/images/action/keyline.gif" height="2" width="100%" align="top"/>
	  </td>
   </tr>
	<tr>
	  <td>
		  <img src="${b.url('my')}" width="100px" align="top"/>
	  </td>
	  <td width="95%">
		 用户名:${user.name}<br/>
		 姓名:${user.fullname}<br/>
		[#if avatar??]
		 文件大小:${avatar.size/1024}KB<br/>
		 更新时间:${(avatar.updatedAt?string("yyyy-MM-dd HH:mm:ss"))!}
		[#else]
		 <em>尚无照片</em>
		[/#if]
	  </td>
   </tr>
</table>

[@b.foot/]