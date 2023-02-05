<html>
    <style>
        .myForm { width:100%; table-layout: fixed; font-family:verdana; font-size:12px; padding:10px; }
        .myForm td { margin:10px; padding:10px; }
        .myForm td:first-child { text-align:right; }
        .myFileInput {
            width:100%;
            height:30px;
        }
        .myInput {
            border-bottom-left-radius: 5px;
            border-top-right-radius: 5px;
            padding-left: 15px;
            height:30px;
            background-color: rgba(250, 250, 210, 0.17);
            border: 1px solid burlywood;
            text-shadow: 1px;
            -webkit-box-shadow: 3px 4px 7px #636363;
        }
        .myCheck { width:30px; height:30px; }
        .singleTd {text-align: left !important; }
        .leftDiv { text-align: right; border-right:1px solid lightblue; }
        .rightDiv { text-align: left; }
        .titleDiv { text-align: center; font-size:200%; }
        .divider { border-top: 2px solid lightgray; height:2px; }
    </style>
    <body>
        <center>            
            <table class="myForm">
                <tr>
                    <td colspan="2"><div class="titleDiv">JOB Manager</div></td>
                </tr>
                <tr>
                    <td colspan="2" class="divider"></td>
                </tr>
                <tr>
                    <td><div class="leftDiv">JobManager rows</div></td><td><div class="rightDiv"><input class="myInput" type="text" id="@{cfg}" /></div></td>
                </tr><tr>
                    <td><div class="leftDiv">Seelcted</div></td><td><div class="rightDiv"><input class="myFileInput" type="file" accept=".war" id="@{file}" /></div></td>
                </tr><tr>
                    <td><div class="leftDiv"></td><td></div><div class="rightDiv"></div></td>
                </tr><tr>
                    <td><div class="leftDiv">Notify</div></td><td><div class="rightDiv"><input class="myCheck" type="checkbox" id="@{notify}" /></div></td>
            </table>
        </center>
    </body>
</html>