from flask import Flask, request, jsonify
from typing import Optional

# 创建 Flask 应用实例
app = Flask(__name__)

# 创建 POST 接口
@app.route('/items', methods=['POST'])
def create_item():
    """
    接收 JSON 数据并返回处理后的结果
    """
    # 获取 JSON 数据
    data = request.get_json()


    print(f"收到的数据: {data}")
    # 构建响应
    response = {
        "code":"ok"
    }
    
    return jsonify(response), 200


# 运行应用: uv run --with flask app.py 
if __name__ == '__main__':
    app.run(debug=True, host='0.0.0.0', port=5000)