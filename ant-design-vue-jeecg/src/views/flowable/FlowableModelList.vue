<template>
  <a-card :bordered="false">

    <!-- 查询区域 -->
    <div class="table-page-search-wrapper">
      <a-form layout="inline">
        <a-row :gutter="24">
          <a-col :md="6" :sm="8">
            <a-form-item label="流程定义ID">
              <a-input placeholder="流程定义ID" v-model="id"></a-input>
            </a-form-item>
          </a-col>
          <a-col :md="6" :sm="8">
            <a-form-item label="流程定义名称">
              <a-input placeholder="流程定义名称" v-model="name"></a-input>
            </a-form-item>
          </a-col>
          <a-col :md="6" :sm="8">
            <span style="float: left;overflow: hidden;" class="table-page-search-submitButtons">
              <a-button type="primary" @click="searchQuery" icon="search">查询</a-button>
              <a-button type="primary" @click="searchReset" icon="reload" style="margin-left: 8px">重置</a-button>
            </span>
          </a-col>

        </a-row>
      </a-form>
    </div>

    <!-- 操作按钮区域 -->
    <div class="table-operator">
      <a-button @click="handleStart()" type="primary" icon="play-square">启动流程</a-button>
    </div>

    <!-- table区域-begin -->
    <div>
      <div class="ant-alert ant-alert-info" style="margin-bottom: 16px;">
        <i class="anticon anticon-info-circle ant-alert-icon"></i> 已选择 <a style="font-weight: 600">{{
        selectedRowKeys.length }}</a>项
        <a style="margin-left: 24px" @click="onClearSelected">清空</a>
      </div>

      <a-table
        ref="table"
        size="middle"
        bordered
        rowKey="id"
        :columns="columns"
        :dataSource="dataSource"
        :loading="loading"
        :rowSelection="{selectedRowKeys: selectedRowKeys, onChange: onSelectChange}">

        <!-- 字符串超长截取省略号显示-->
        <span slot="esContent" slot-scope="text">
          <j-ellipsis :value="text" :length="10" />
        </span>

        <span slot="action" slot-scope="text, record">
          <a href="javascript:;" @click="handleStart(record)">启动</a>
        </span>
      </a-table>
    </div>
    <!-- table区域-end -->

    <!-- 表单区域 -->
  </a-card>
</template>

<script>
  import {getAction,postAction} from '@/api/manage'
  export default {
    name: "FlowableModelList",
    data() {
      return {
        loading:false,
        selectedRowKeys: [],
        /* table选中records*/
        selectionRows: [],
        id:'',
        name:'',
        dataSource:[],
        description: '消息管理页面',
        // 新增修改按钮是否显示
        show: false,
        // 表头
        columns: [
          {
            title: '序号',
            dataIndex: '',
            key: 'rowIndex',
            width: 60,
            align: "center",
            customRender: function (t, r, index) {
              return parseInt(index) + 1;
            }
          },
          {
            title: '流程定义ID',
            align: "center",
            dataIndex: 'id'
          },
          {
            title: '流程定义key',
            align: "center",
            dataIndex: 'key'
          },
          {
            title: '流程定义名称',
            align: "center",
            dataIndex: 'name'
          },
          {
            title: '流程定义版本',
            align: "center",
            dataIndex: 'version'
          },
          {
            title: '操作',
            dataIndex: 'action',
            align: "center",
            scopedSlots: {customRender: 'action'},
          }
        ],
        url: {
          list: "/flowable/load/process-definition",
          start:"/flowable/start/process-instance",
        },
      }
    },
    created() {
      this.loadData();
    },
    methods: {
      //加载数据源
      loadData(){
        this.loading = true;
        getAction(this.url.list, {id:this.id,name:this.name}).then((res) => {
          if (res) {
            this.dataSource = res;
            this.loading = false;
          }
        })
      },
      //查询
      searchQuery(){
        this.loadData();
      },
      //重置
      searchReset(){
        this.id='';
        this.name='';
        this.loadData();
      },
      //清除选中
      onClearSelected() {
        this.selectedRowKeys = [];
        this.selectionRows = [];
      },
      //选择变化
      onSelectChange(selectedRowKeys, selectionRows) {
        this.selectedRowKeys = selectedRowKeys;
        this.selectionRows = selectionRows;
      },
      //流程启动
      handleStart(recode){
        var this_ = this;
        var selectedData=[];
        this_.loading = true;
        if(recode){//判断是否批量启动
          selectedData.push(recode);
        }else
          selectedData = this_.selectionRows;
        if(selectedData.length<=0){
          this_.loading = false;
          return this_.$message.warning("请选择需要启动的流程定义！");
        }

        postAction(this_.url.start, {processDefinitionModelList:selectedData}).then((res) => {
          if (res.success) {
            this_.$message.success(res.message);
          }else
            this_.$message.error(res.message);
        }).finally(data =>{
          selectedData=[];
          this_.loading = false;
        })
      },
    }
  }
</script>
<style lang="less" scoped>
  /** Button按钮间距 */
  .ant-btn {
    margin-left: 3px
  }

  .ant-card-body .table-operator {
    margin-bottom: 18px;
  }

  .ant-table-tbody .ant-table-row td {
    padding-top: 15px;
    padding-bottom: 15px;
  }

  .anty-row-operator button {
    margin: 0 5px
  }

  .ant-btn-danger {
    background-color: #ffffff
  }

  .ant-modal-cust-warp {
    height: 100%
  }

  .ant-modal-cust-warp .ant-modal-body {
    height: calc(100% - 110px) !important;
    overflow-y: auto
  }

  .ant-modal-cust-warp .ant-modal-content {
    height: 90% !important;
    overflow-y: hidden
  }
</style>