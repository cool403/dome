from datetime import datetime
import json
import sqlite3
import tkinter as tk
from tkinter import filedialog, messagebox, ttk
from tkinter.scrolledtext import ScrolledText
from typing import override

import pygments
from pygments import highlight
from pygments.formatters import TerminalFormatter
from pygments.lexers import JsonLexer
import sv_ttk


class JsonText(ScrolledText):
    """支持JSON语法高亮的文本框"""

    def __init__(self, master=None, **kwargs):
        super().__init__(master, **kwargs)
        self.lexer = JsonLexer()
        self.formatter = TerminalFormatter()  # 使用TerminalFormatter代替

    def highlight(self):
        code = self.get("1.0", "end-1c")
        try:
            parsed = json.loads(code)
            code = json.dumps(parsed, indent=2, ensure_ascii=False)
            self.delete("1.0", "end")
            self.insert("1.0", code)

            # 使用tag来模拟高亮
            self.tag_configure("Token", foreground="black")
            self.tag_configure("Token.String", foreground="green")
            self.tag_configure("Token.Number", foreground="blue")
            self.tag_configure("Token.Keyword", foreground="purple")
            self.tag_configure("Token.Name.Tag", foreground="red")

            # 简单的关键字高亮
            keywords = ["true", "false", "null"]
            for kw in keywords:
                start = "1.0"
                while True:
                    pos = self.search(kw, start, stopindex="end")
                    if not pos:
                        break
                    end = f"{pos}+{len(kw)}c"
                    self.tag_add("Token.Keyword", pos, end)
                    start = end

        except json.JSONDecodeError:
            pass


class MockApp:
    def __init__(self, root):
        self.root = root
        self.root.title("流量Mock工具")
        self.root.geometry("1200x800")

        # 设置Sun Valley主题
        sv_ttk.set_theme("dark")  # 使用浅色主题

        # 数据库连接
        self.db_conn = None
        self.db_path = None

        # 创建主界面
        self.create_welcome_screen()

    def create_welcome_screen(self):
        """创建欢迎界面"""
        self.clear_content()

        # 主框架
        main_frame = ttk.Frame(self.root, padding=50)
        main_frame.pack(fill="both", expand=True)

        # 标题
        ttk.Label(main_frame,
                  text="流量Mock工具",
                  font=("Segoe UI", 24, "bold")).pack(pady=20)

        # 描述
        ttk.Label(main_frame,
                  text="选择SQLite数据库文件开始",
                  font=("Segoe UI", 12)).pack(pady=10)

        # 选择文件按钮
        file_frame = ttk.Frame(main_frame)
        file_frame.pack(pady=30)

        self.db_path_var = tk.StringVar()
        ttk.Entry(file_frame,
                  textvariable=self.db_path_var,
                  width=50,
                  state="readonly").pack(side="left", padx=5)

        ttk.Button(file_frame,
                   text="选择文件",
                   command=self.select_db_file).pack(side="left", padx=5)

        # 确认按钮
        ttk.Button(main_frame,
                   text="确认",
                   style="Accent.TButton",
                   command=self.open_db).pack(pady=20)

    def select_db_file(self):
        """选择数据库文件"""
        file_path = filedialog.askopenfilename(
            title="选择SQLite数据库文件",
            filetypes=[("SQLite数据库", "*.db *.sqlite"), ("所有文件", "*.*")]
        )
        if file_path:
            self.db_path_var.set(file_path)

    def open_db(self):
        """打开数据库"""
        db_path = self.db_path_var.get()
        if not db_path:
            messagebox.showerror("错误", "请选择数据库文件")
            return

        try:
            conn = sqlite3.connect(db_path)
            print('数据库连接成功.')
            cursor = conn.cursor()

            # 检查表是否存在
            cursor.execute(
                "SELECT name FROM sqlite_master WHERE type='table' AND name IN ('api_records', 'api_configs')")
            tables = cursor.fetchall()
            print(tables)
            if len(tables) != 2:
                messagebox.showerror("错误", "数据库缺少必要的表结构")
                return

            self.db_conn = conn
            self.db_path = db_path

            # 创建主应用界面
            self.create_main_interface()

        except Exception as e:
            messagebox.showerror("错误", f"无法打开数据库: {str(e)}")

    def create_main_interface(self):
        """创建主应用界面"""
        self.clear_content()

        # 创建主框架
        self.main_frame = ttk.Frame(self.root)
        self.main_frame.pack(fill="both", expand=True, padx=10, pady=10)

        # 创建笔记本(选项卡)
        self.notebook = ttk.Notebook(self.main_frame)
        self.notebook.pack(fill="both", expand=True)

        # 请求记录标签页
        self.create_records_tab()

        # 接口配置标签页
        self.create_configs_tab()

        # 底部状态栏
        self.status_var = tk.StringVar()
        self.status_var.set(f"数据库文件位置: {self.db_path}")

        status_bar = ttk.Frame(self.main_frame, height=25)
        status_bar.pack(fill="x", pady=(5, 0))
        ttk.Label(status_bar,
                  textvariable=self.status_var,
                  relief="sunken",
                  anchor="w").pack(fill="x")

    def create_records_tab(self):
        """创建请求记录标签页"""
        tab = ttk.Frame(self.notebook)
        self.notebook.add(tab, text="请求记录")

        # 工具栏
        toolbar = ttk.Frame(tab)
        toolbar.pack(fill="x", pady=(0, 10))

        # 刷新按钮
        ttk.Button(toolbar,
                   text="刷新",
                   command=self.refresh_records).pack(side="left", padx=5)

        # 搜索框
        search_frame = ttk.Frame(toolbar)
        search_frame.pack(side="right", padx=5)

        self.record_search_var = tk.StringVar()
        ttk.Entry(search_frame,
                  textvariable=self.record_search_var,
                  width=30).pack(side="left", padx=5)

        ttk.Button(search_frame,
                   text="搜索",
                   command=self.search_records).pack(side="left")

        # 记录表格
        columns = ("id", "http_url", "http_method",
                   "http_status", "req_time", "api_type")
        self.records_tree = ttk.Treeview(
            tab,
            columns=columns,
            show="headings",
            selectmode="browse"
        )

        # 设置列
        self.records_tree.heading("id", text="ID")
        self.records_tree.heading("http_url", text="URL")
        self.records_tree.heading("http_method", text="方法")
        self.records_tree.heading("http_status", text="状态码")
        self.records_tree.heading("req_time", text="请求时间")
        self.records_tree.heading("api_type", text="类型")

        self.records_tree.column("id", width=50, anchor="center")
        self.records_tree.column("http_url", width=400)
        self.records_tree.column("http_method", width=80, anchor="center")
        self.records_tree.column("http_status", width=80, anchor="center")
        self.records_tree.column("req_time", width=180)
        self.records_tree.column("api_type", width=80, anchor="center")

        # 滚动条
        scrollbar = ttk.Scrollbar(
            tab, orient="vertical", command=self.records_tree.yview)
        self.records_tree.configure(yscrollcommand=scrollbar.set)

        # 布局
        self.records_tree.pack(side="left", fill="both", expand=True)
        scrollbar.pack(side="right", fill="y")

        # 绑定双击事件查看详情
        self.records_tree.bind("<Double-1>", self.show_record_detail)

        # 初始加载数据
        self.refresh_records()

    def refresh_records(self):
        """刷新请求记录"""
        if not self.db_conn:
            return

        cursor = self.db_conn.cursor()
        cursor.execute("""
            SELECT id, http_url, http_method, http_status, 
                   strftime('%Y-%m-%d %H:%M:%S', req_time) as req_time, api_type
            FROM api_records
            ORDER BY req_time DESC
            LIMIT 100
        """)

        # 清空现有数据
        for item in self.records_tree.get_children():
            self.records_tree.delete(item)

        # 添加新数据
        for row in cursor.fetchall():
            self.records_tree.insert("", "end", values=row)

    def search_records(self):
        """搜索请求记录"""
        keyword = self.record_search_var.get().strip()
        if not keyword:
            self.refresh_records()
            return

        if not self.db_conn:
            return

        cursor = self.db_conn.cursor()
        cursor.execute("""
            SELECT id, http_url, http_method, http_status, 
                   strftime('%Y-%m-%d %H:%M:%S', req_time) as req_time, api_type
            FROM api_records
            WHERE http_url LIKE ? OR http_method LIKE ? OR trace_id LIKE ?
            ORDER BY req_time DESC
            LIMIT 100
        """, (f"%{keyword}%", f"%{keyword}%", f"%{keyword}%"))

        # 清空现有数据
        for item in self.records_tree.get_children():
            self.records_tree.delete(item)

        # 添加搜索结果
        for row in cursor.fetchall():
            self.records_tree.insert("", "end", values=row)

    def show_record_detail(self, event):
        """显示请求记录详情"""
        selected = self.records_tree.selection()
        if not selected:
            return

        item = self.records_tree.item(selected[0])
        record_id = item["values"][0]

        # 查询完整记录
        cursor = self.db_conn.cursor()
        cursor.execute("""
            SELECT * FROM api_records WHERE id = ?
        """, (record_id,))
        record = cursor.fetchone()

        if not record:
            messagebox.showerror("错误", "找不到该记录")
            return

        # 创建详情窗口
        detail_window = tk.Toplevel(self.root)
        detail_window.title(f"请求详情 - ID: {record_id}")
        detail_window.geometry("1000x800")

        # 创建笔记本(选项卡)
        notebook = ttk.Notebook(detail_window)
        notebook.pack(fill="both", expand=True, padx=10, pady=10)

        # 基本信息标签页
        basic_tab = ttk.Frame(notebook)
        notebook.add(basic_tab, text="基本信息")

        # 基本信息表格
        basic_info = [
            ("ID", record[0]),
            ("URL", record[1]),
            ("HTTP方法", record[2]),
            ("请求时间", record[7]),
            ("响应时间", record[8]),
            ("HTTP状态码", record[9]),
            ("API类型", record[12]),
            ("Trace ID", record[6])
        ]

        for i, (label, value) in enumerate(basic_info):
            ttk.Label(basic_tab, text=label, font=("Segoe UI", 10, "bold")).grid(
                row=i, column=0, sticky="e", padx=5, pady=2)
            ttk.Label(basic_tab, text=value if value else "N/A").grid(
                row=i, column=1, sticky="w", padx=5, pady=2)

        # 请求参数标签页
        request_tab = ttk.Frame(notebook)
        notebook.add(request_tab, text="请求参数")

        ttk.Label(request_tab, text="查询参数:", font=(
            "Segoe UI", 10, "bold")).pack(anchor="w", pady=(5, 0))
        query_params_text = JsonText(
            request_tab, wrap="none", width=120, height=5)
        query_params_text.pack(fill="x", padx=5, pady=5)
        query_params_text.insert("1.0", record[3] if record[3] else "{}")
        query_params_text.highlight()

        ttk.Label(request_tab, text="请求头:", font=(
            "Segoe UI", 10, "bold")).pack(anchor="w", pady=(5, 0))
        headers_text = JsonText(request_tab, wrap="none", width=120, height=5)
        headers_text.pack(fill="x", padx=5, pady=5)
        headers_text.insert("1.0", record[10] if record[10] else "{}")
        headers_text.highlight()

        ttk.Label(request_tab, text="请求体:", font=(
            "Segoe UI", 10, "bold")).pack(anchor="w", pady=(5, 0))
        request_body_text = JsonText(
            request_tab, wrap="none", width=120, height=10)
        request_body_text.pack(fill="both", expand=True, padx=5, pady=5)
        request_body_text.insert("1.0", record[4] if record[4] else "")
        request_body_text.highlight()

        # 响应信息标签页
        response_tab = ttk.Frame(notebook)
        notebook.add(response_tab, text="响应信息")

        ttk.Label(response_tab, text="响应头:", font=(
            "Segoe UI", 10, "bold")).pack(anchor="w", pady=(5, 0))
        response_headers_text = JsonText(
            response_tab, wrap="none", width=120, height=5)
        response_headers_text.pack(fill="x", padx=5, pady=5)
        response_headers_text.insert("1.0", record[11] if record[11] else "{}")
        response_headers_text.highlight()

        ttk.Label(response_tab, text="响应体:", font=(
            "Segoe UI", 10, "bold")).pack(anchor="w", pady=(5, 0))
        response_body_text = JsonText(
            response_tab, wrap="none", width=120, height=15)
        response_body_text.pack(fill="both", expand=True, padx=5, pady=5)
        response_body_text.insert("1.0", record[5] if record[5] else "")
        response_body_text.highlight()

    def create_configs_tab(self):
        """创建接口配置标签页"""
        tab = ttk.Frame(self.notebook)
        self.notebook.add(tab, text="接口配置")

        # 工具栏
        toolbar = ttk.Frame(tab)
        toolbar.pack(fill="x", pady=(0, 10))

        # 添加按钮
        ttk.Button(toolbar,
                   text="添加配置",
                   style="Accent.TButton",
                   command=self.add_config).pack(side="left", padx=5)

        # 刷新按钮
        ttk.Button(toolbar,
                   text="刷新",
                   command=self.refresh_configs).pack(side="left", padx=5)

        # 搜索框
        search_frame = ttk.Frame(toolbar)
        search_frame.pack(side="right", padx=5)

        self.config_search_var = tk.StringVar()
        ttk.Entry(search_frame,
                  textvariable=self.config_search_var,
                  width=30).pack(side="left", padx=5)

        ttk.Button(search_frame,
                   text="搜索",
                   command=self.search_configs).pack(side="left")

        # 配置表格
        columns = ("id", "http_url", "http_method", "is_mock_enabled",
                   "mock_type", "api_type", "description")
        self.configs_tree = ttk.Treeview(
            tab,
            columns=columns,
            show="headings",
            selectmode="browse"
        )

        # 设置列
        self.configs_tree.heading("id", text="ID")
        self.configs_tree.heading("http_url", text="URL")
        self.configs_tree.heading("http_method", text="方法")
        self.configs_tree.heading("is_mock_enabled", text="Mock启用")
        self.configs_tree.heading("mock_type", text="Mock类型")
        self.configs_tree.heading("api_type", text="API类型")
        self.configs_tree.heading("description", text="描述")

        self.configs_tree.column("id", width=50, anchor="center")
        self.configs_tree.column("http_url", width=350)
        self.configs_tree.column("http_method", width=80, anchor="center")
        self.configs_tree.column("is_mock_enabled", width=100, anchor="center")
        self.configs_tree.column("mock_type", width=100, anchor="center")
        self.configs_tree.column("api_type", width=80, anchor="center")
        self.configs_tree.column("description", width=200)

        # 滚动条
        scrollbar = ttk.Scrollbar(
            tab, orient="vertical", command=self.configs_tree.yview)
        self.configs_tree.configure(yscrollcommand=scrollbar.set)

        # 布局
        self.configs_tree.pack(side="left", fill="both", expand=True)
        scrollbar.pack(side="right", fill="y")

        # 绑定双击事件编辑配置
        self.configs_tree.bind("<Double-1>", self.edit_config)

        # 右键菜单
        self.setup_config_context_menu()

        # 初始加载数据
        self.refresh_configs()

    def setup_config_context_menu(self):
        """设置配置表格的右键菜单"""
        def show_context_menu(event):
            item = self.configs_tree.identify_row(event.y)
            if item:
                self.configs_tree.selection_set(item)
                menu = tk.Menu(self.root, tearoff=0)
                menu.add_command(
                    label="编辑", command=lambda: self.edit_config(None))
                menu.add_command(label="删除", command=self.delete_config)
                menu.post(event.x_root, event.y_root)

        self.configs_tree.bind("<Button-3>", show_context_menu)

    def refresh_configs(self):
        """刷新接口配置"""
        if not self.db_conn:
            return

        cursor = self.db_conn.cursor()
        cursor.execute("""
            SELECT id, http_url, http_method, 
                   CASE WHEN is_mock_enabled THEN '是' ELSE '否' END as is_mock_enabled,
                   mock_type, api_type, description
            FROM api_configs
            ORDER BY updated_at DESC
        """)

        # 清空现有数据
        for item in self.configs_tree.get_children():
            self.configs_tree.delete(item)

        # 添加新数据
        for row in cursor.fetchall():
            self.configs_tree.insert("", "end", values=row)

    def search_configs(self):
        """搜索接口配置"""
        keyword = self.config_search_var.get().strip()
        if not keyword:
            self.refresh_configs()
            return

        if not self.db_conn:
            return

        cursor = self.db_conn.cursor()
        cursor.execute("""
            SELECT id, http_url, http_method, 
                   CASE WHEN is_mock_enabled THEN '是' ELSE '否' END as is_mock_enabled,
                   mock_type, api_type, description
            FROM api_configs
            WHERE http_url LIKE ? OR http_method LIKE ? OR description LIKE ?
            ORDER BY updated_at DESC
        """, (f"%{keyword}%", f"%{keyword}%", f"%{keyword}%"))

        # 清空现有数据
        for item in self.configs_tree.get_children():
            self.configs_tree.delete(item)

        # 添加搜索结果
        for row in cursor.fetchall():
            self.configs_tree.insert("", "end", values=row)

    def add_config(self):
        """添加接口配置"""
        self.show_config_editor()

    def edit_config(self, event):
        """编辑接口配置"""
        selected = self.configs_tree.selection()
        if not selected:
            return

        item = self.configs_tree.item(selected[0])
        config_id = item["values"][0]

        # 查询配置详情
        cursor = self.db_conn.cursor()
        cursor.execute("SELECT * FROM api_configs WHERE id = ?", (config_id,))
        config = cursor.fetchone()

        if not config:
            messagebox.showerror("错误", "找不到该配置")
            return

        self.show_config_editor(config)

    def show_config_editor(self, config=None):
        """显示配置编辑器"""
        editor_window = tk.Toplevel(self.root)
        editor_window.title("添加配置" if config is None else "编辑配置")
        editor_window.geometry("800x600")

        # 表单框架
        form_frame = ttk.Frame(editor_window, padding=20)
        form_frame.pack(fill="both", expand=True)

        # 表单变量
        self.editor_vars = {
            "id": tk.IntVar(value=config[0] if config else 0),
            "http_url": tk.StringVar(value=config[1] if config else ""),
            "host": tk.StringVar(value=config[2] if config else ""),
            "http_method": tk.StringVar(value=config[3] if config else "GET"),
            "is_mock_enabled": tk.BooleanVar(value=bool(config[4]) if config else False),
            "mock_type": tk.StringVar(value=config[5] if config else "STATIC"),
            "static_response": tk.StringVar(value=config[6] if config else "{}"),
            "dynamic_rule": tk.StringVar(value=config[7] if config else "{}"),
            "replay_record_id": tk.IntVar(value=config[8] if config else 0),
            "delay": tk.IntVar(value=config[9] if config else 0),
            "description": tk.StringVar(value=config[10] if config else ""),
            "api_type": tk.StringVar(value=config[11] if config else "INT")
        }

        # 表单行计数器
        row = 0

        # URL
        ttk.Label(form_frame, text="URL路径:", font=("Segoe UI", 10, "bold")).grid(
            row=row, column=0, sticky="e", padx=5, pady=5)
        ttk.Entry(form_frame, textvariable=self.editor_vars["http_url"], width=50).grid(
            row=row, column=1, sticky="w", padx=5, pady=5)
        row += 1

        # Host
        ttk.Label(form_frame, text="Host:", font=("Segoe UI", 10, "bold")).grid(
            row=row, column=0, sticky="e", padx=5, pady=5)
        ttk.Entry(form_frame, textvariable=self.editor_vars["host"]).grid(
            row=row, column=1, sticky="w", padx=5, pady=5)
        row += 1

        # HTTP方法和API类型
        method_frame = ttk.Frame(form_frame)
        method_frame.grid(row=row, column=1, sticky="w", pady=5)

        ttk.Label(form_frame, text="HTTP方法:", font=("Segoe UI", 10, "bold")).grid(
            row=row, column=0, sticky="e", padx=5, pady=5)

        methods = ["GET", "POST", "PUT", "DELETE", "PATCH", "HEAD", "OPTIONS"]
        ttk.Combobox(method_frame,
                     textvariable=self.editor_vars["http_method"],
                     values=methods,
                     width=10).pack(side="left", padx=5)

        ttk.Label(method_frame, text="API类型:").pack(side="left", padx=10)
        ttk.Combobox(method_frame,
                     textvariable=self.editor_vars["api_type"],
                     values=["INT", "EXT"],
                     width=5).pack(side="left")
        row += 1

        # Mock启用
        ttk.Checkbutton(form_frame,
                        text="启用Mock",
                        variable=self.editor_vars["is_mock_enabled"]).grid(
            row=row, column=1, sticky="w", padx=5, pady=5)
        row += 1

        # Mock类型
        ttk.Label(form_frame, text="Mock类型:", font=("Segoe UI", 10, "bold")).grid(
            row=row, column=0, sticky="e", padx=5, pady=5)

        mock_types = ["STATIC", "DYNAMIC", "REPLAY", "PROXY"]
        mock_type_cb = ttk.Combobox(form_frame,
                                    textvariable=self.editor_vars["mock_type"],
                                    values=mock_types,
                                    state="readonly")
        mock_type_cb.grid(row=row, column=1, sticky="w", padx=5, pady=5)
        mock_type_cb.bind("<<ComboboxSelected>>", self.update_mock_type_fields)
        row += 1

        # 延迟
        ttk.Label(form_frame, text="延迟(ms):", font=("Segoe UI", 10, "bold")).grid(
            row=row, column=0, sticky="e", padx=5, pady=5)
        ttk.Spinbox(form_frame,
                    from_=0,
                    to=10000,
                    increment=100,
                    textvariable=self.editor_vars["delay"]).grid(
            row=row, column=1, sticky="w", padx=5, pady=5)
        row += 1

        # Mock配置区域
        self.mock_config_frame = ttk.Frame(form_frame)
        self.mock_config_frame.grid(
            row=row, column=0, columnspan=2, sticky="nsew", pady=10)
        row += 1

        # 描述
        ttk.Label(form_frame, text="描述:", font=("Segoe UI", 10, "bold")).grid(
            row=row, column=0, sticky="ne", padx=5, pady=5)
        ttk.Entry(form_frame,
                  textvariable=self.editor_vars["description"],
                  width=50).grid(row=row, column=1, sticky="w", padx=5, pady=5)
        row += 1

        # 按钮区域
        button_frame = ttk.Frame(form_frame)
        button_frame.grid(row=row, column=0, columnspan=2, pady=20)

        ttk.Button(button_frame,
                   text="保存",
                   style="Accent.TButton",
                   command=lambda: self.save_config(editor_window)).pack(side="left", padx=10)

        ttk.Button(button_frame,
                   text="取消",
                   command=editor_window.destroy).pack(side="left", padx=10)

        # 初始更新Mock类型字段
        self.update_mock_type_fields()

        # 配置网格权重
        form_frame.grid_rowconfigure(row-2, weight=1)
        form_frame.grid_columnconfigure(1, weight=1)

    def update_mock_type_fields(self, event=None):
        """根据Mock类型更新显示的字段"""
        for widget in self.mock_config_frame.winfo_children():
            widget.destroy()

        mock_type = self.editor_vars["mock_type"].get()
        row = 0

        if mock_type == "STATIC":
            ttk.Label(self.mock_config_frame, text="静态响应:", font=("Segoe UI", 10, "bold")).grid(
                row=row, column=0, sticky="ne", padx=5, pady=5)
            row += 1

            static_response_text = JsonText(
                self.mock_config_frame, wrap="none", width=80, height=15)
            static_response_text.grid(
                row=row, column=0, columnspan=2, sticky="nsew", padx=5, pady=5)
            static_response_text.insert(
                "1.0", self.editor_vars["static_response"].get())
            static_response_text.highlight()

            # 保存引用以便获取值
            self.static_response_text = static_response_text

        elif mock_type == "DYNAMIC":
            ttk.Label(self.mock_config_frame, text="动态规则:", font=("Segoe UI", 10, "bold")).grid(
                row=row, column=0, sticky="ne", padx=5, pady=5)
            row += 1

            dynamic_rule_text = JsonText(
                self.mock_config_frame, wrap="none", width=80, height=15)
            dynamic_rule_text.grid(
                row=row, column=0, columnspan=2, sticky="nsew", padx=5, pady=5)
            dynamic_rule_text.insert(
                "1.0", self.editor_vars["dynamic_rule"].get())
            dynamic_rule_text.highlight()

            # 保存引用以便获取值
            self.dynamic_rule_text = dynamic_rule_text

        elif mock_type == "REPLAY":
            ttk.Label(self.mock_config_frame, text="选择记录:", font=("Segoe UI", 10, "bold")).grid(
                row=row, column=0, sticky="ne", padx=5, pady=5)
            row += 1

            # 获取匹配的记录
            url = self.editor_vars["http_url"].get()
            method = self.editor_vars["http_method"].get()

            records = self.get_matching_records(url, method)

            if records:
                record_var = tk.StringVar()
                record_cb = ttk.Combobox(self.mock_config_frame,
                                         textvariable=record_var,
                                         values=[
                                             f"{r[0]} - {r[1]} ({r[2]})" for r in records],
                                         state="readonly")
                record_cb.grid(row=row, column=0, columnspan=2,
                               sticky="ew", padx=5, pady=5)

                if records:
                    record_cb.current(0)
                    self.editor_vars["replay_record_id"].set(records[0][0])

                record_cb.bind("<<ComboboxSelected>>", lambda e: self.editor_vars["replay_record_id"].set(
                    records[record_cb.current()][0]))
            else:
                ttk.Label(self.mock_config_frame, text="没有找到匹配的记录").grid(
                    row=row, column=0, columnspan=2, sticky="w", padx=5, pady=5)

        # 配置网格权重
        self.mock_config_frame.grid_rowconfigure(row, weight=1)
        self.mock_config_frame.grid_columnconfigure(1, weight=1)

    def get_matching_records(self, url, method):
        """获取匹配URL和方法的记录"""
        if not self.db_conn or not url or not method:
            return []

        cursor = self.db_conn.cursor()
        cursor.execute("""
            SELECT id, http_url, http_method 
            FROM api_records 
            WHERE http_url = ? AND http_method = ?
            ORDER BY req_time DESC
            LIMIT 50
        """, (url, method))

        return cursor.fetchall()

    def save_config(self, window):
        """保存配置"""
        try:
            config_data = {
                "http_url": self.editor_vars["http_url"].get().strip(),
                "host": self.editor_vars["host"].get().strip(),
                "http_method": self.editor_vars["http_method"].get(),
                "is_mock_enabled": int(self.editor_vars["is_mock_enabled"].get()),
                "mock_type": self.editor_vars["mock_type"].get(),
                "static_response": self.static_response_text.get("1.0", "end-1c")
                if hasattr(self, 'static_response_text') else "",
                "dynamic_rule": self.dynamic_rule_text.get("1.0", "end-1c")
                if hasattr(self, 'dynamic_rule_text') else "",
                "replay_record_id": self.editor_vars["replay_record_id"].get(),
                "delay": self.editor_vars["delay"].get(),
                "description": self.editor_vars["description"].get().strip(),
                "api_type": self.editor_vars["api_type"].get(),
                "updated_at": datetime.now().strftime("%Y-%m-%d %H:%M:%S")
            }

            # 验证必填字段
            if not config_data["http_url"]:
                messagebox.showerror("错误", "URL路径不能为空")
                return

            cursor = self.db_conn.cursor()

            if self.editor_vars["id"].get() == 0:  # 新增
                cursor.execute("""
                    INSERT INTO api_configs (
                        http_url, host, http_method, is_mock_enabled, mock_type,
                        static_response, dynamic_rule, replay_record_id, delay,
                        description, api_type, created_at, updated_at
                    ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, datetime('now'), ?)
                """, (
                    config_data["http_url"],
                    config_data["host"],
                    config_data["http_method"],
                    config_data["is_mock_enabled"],
                    config_data["mock_type"],
                    config_data["static_response"],
                    config_data["dynamic_rule"],
                    config_data["replay_record_id"],
                    config_data["delay"],
                    config_data["description"],
                    config_data["api_type"],
                    config_data["updated_at"]
                ))
            else:  # 更新
                cursor.execute("""
                    UPDATE api_configs SET
                        http_url = ?,
                        host = ?,
                        http_method = ?,
                        is_mock_enabled = ?,
                        mock_type = ?,
                        static_response = ?,
                        dynamic_rule = ?,
                        replay_record_id = ?,
                        delay = ?,
                        description = ?,
                        api_type = ?,
                        updated_at = ?
                    WHERE id = ?
                """, (
                    config_data["http_url"],
                    config_data["host"],
                    config_data["http_method"],
                    config_data["is_mock_enabled"],
                    config_data["mock_type"],
                    config_data["static_response"],
                    config_data["dynamic_rule"],
                    config_data["replay_record_id"],
                    config_data["delay"],
                    config_data["description"],
                    config_data["api_type"],
                    config_data["updated_at"],
                    self.editor_vars["id"].get()
                ))

            self.db_conn.commit()
            messagebox.showinfo("成功", "配置保存成功")
            window.destroy()
            self.refresh_configs()

        except Exception as e:
            messagebox.showerror("错误", f"保存失败: {str(e)}")

    def delete_config(self):
        """删除配置"""
        selected = self.configs_tree.selection()
        if not selected:
            return

        item = self.configs_tree.item(selected[0])
        config_id = item["values"][0]

        if not messagebox.askyesno("确认", "确定要删除该配置吗？"):
            return

        try:
            cursor = self.db_conn.cursor()
            cursor.execute(
                "DELETE FROM api_configs WHERE id = ?", (config_id,))
            self.db_conn.commit()

            messagebox.showinfo("成功", "配置删除成功")
            self.refresh_configs()
        except Exception as e:
            messagebox.showerror("错误", f"删除失败: {str(e)}")

    def clear_content(self):
        """清空主内容区域"""
        for widget in self.root.winfo_children():
            widget.destroy()


if __name__ == "__main__":
    root = tk.Tk()
    app = MockApp(root)
    root.mainloop()
