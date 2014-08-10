package com.ElasticScrollView.view;

import java.util.Date;

import com.ElasticScrollView.cjy.R;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

public class ElasticScrollView extends ScrollView {
	private static final String TAG = "ElasticScrollView";
	private final static int RELEASE_To_REFRESH = 0;
	private final static int PULL_To_REFRESH = 1;
	private final static int REFRESHING = 2;
	private final static int DONE = 3;
	private final static int LOADING = 4;
	// ʵ�ʵ�padding�ľ����������ƫ�ƾ���ı���
	private final static int RATIO = 3;

	private int headContentWidth;
	private int headContentHeight;

	private LinearLayout innerLayout;
	private LinearLayout headView;
	private ImageView arrowImageView;//��ͷͼƬ
	private ProgressBar progressBar;
	private TextView tipsTextview;// ��ʾ���� �ɿ� ˢ�µ�����
	private TextView lastUpdatedTextView;
	private OnRefreshListener refreshListener;
	private boolean isRefreshable;
	private int state;
	/**
	 * �������������������������ʱ ��arrowImageViewͼƬ�����ж����ı䶯��
	 */
	private boolean isBack;

	private RotateAnimation animation; //��ͷ�Ķ���
	private RotateAnimation reverseAnimation;

	private boolean canReturn; //������ʾ�ɿ��ֺ�Ϳ��Իص���
	private boolean isRecored;
	private int startY;

	public ElasticScrollView(Context context) {
		super(context);
		init(context);
	}

	public ElasticScrollView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	private void init(Context context) {
		LayoutInflater inflater = LayoutInflater.from(context);
		innerLayout = new LinearLayout(context);
		innerLayout.setLayoutParams(new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.FILL_PARENT,
				LinearLayout.LayoutParams.WRAP_CONTENT));
		innerLayout.setOrientation(LinearLayout.VERTICAL);
		
		/**
		 * ����ʱ ��ʾ�Ĳ���
		 */
		headView = (LinearLayout) inflater.inflate(R.layout.mylistview_head,
				null);
		
		arrowImageView = (ImageView) headView
				.findViewById(R.id.head_arrowImageView);
		progressBar = (ProgressBar) headView
				.findViewById(R.id.head_progressBar);
		
		/**
		 * ��ʾ���� �ɿ� ˢ�µ�����
		 */
		tipsTextview = (TextView) headView.findViewById(R.id.head_tipsTextView);
		lastUpdatedTextView = (TextView) headView
				.findViewById(R.id.head_lastUpdatedTextView);
		/**
		 * 
		 */
		measureView(headView);

		/**
		 * ��ȡ����headerview�Ĵ�С�Ϳ�Ȼ������paddingֵ��Ȼ����һ��ʼ
		 * ����ʾ��screen
		 */
		headContentHeight = headView.getMeasuredHeight();
		headContentWidth = headView.getMeasuredWidth();
		headView.setPadding(0, -1 * headContentHeight, 0, 0);
		headView.invalidate();

		Log.i("size", "width:" + headContentWidth + " height:"
				+ headContentHeight);
		//��headerView��ӵ��ڲ���linearlayout��
		innerLayout.addView(headView);
		/**
		 * ��Linearlayout��ӵ���ǰ��ScrollView��
		 */
		addView(innerLayout);

		animation = new RotateAnimation(0, -180,
				RotateAnimation.RELATIVE_TO_SELF, 0.5f,
				RotateAnimation.RELATIVE_TO_SELF, 0.5f);
		animation.setInterpolator(new LinearInterpolator());
		animation.setDuration(250);
		animation.setFillAfter(true);

		reverseAnimation = new RotateAnimation(-180, 0,
				RotateAnimation.RELATIVE_TO_SELF, 0.5f,
				RotateAnimation.RELATIVE_TO_SELF, 0.5f);
		reverseAnimation.setInterpolator(new LinearInterpolator());
		reverseAnimation.setDuration(200);
		reverseAnimation.setFillAfter(true);

		state = DONE;
		isRefreshable = false;
		canReturn = false;
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (isRefreshable) {
			switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				if (getScrollY() == 0 && !isRecored) {
					isRecored = true;
					startY = (int) event.getY();
					Log.i(TAG, "��downʱ���¼��ǰλ�á�");
					LogUtils2.i("��downʱ���¼��ǰλ�á�==startY="+startY);
				}
				break;

			case MotionEvent.ACTION_MOVE:
				int tempY = (int) event.getY();
				
				if (!isRecored && getScrollY() == 0) {
					Log.i(TAG, "��moveʱ���¼��λ��");
					isRecored = true;
					startY = tempY;
				}
				
				LogUtils2.d("tempY=="+tempY+"   startY=="+startY);
				/**
				 *  done״̬��  һ��ʼ��״̬
				 */
				if (state == DONE) {
					if (tempY - startY > 0) {
						state = PULL_To_REFRESH;
						changeHeaderViewByState();
					}
				}

				/**
				 *  ��û�е�����ʾ�ɿ�ˢ�µ�ʱ��,DONE������PULL_To_REFRESH״̬
				 */
				if (state == PULL_To_REFRESH) {
					canReturn = true;

					// ���������Խ���RELEASE_TO_REFRESH��״̬
					if ((tempY - startY) / RATIO >= headContentHeight) {
						state = RELEASE_To_REFRESH;
						isBack = true;
						changeHeaderViewByState();
						LogUtils2.i("��done��������ˢ��״̬ת�䵽�ɿ�ˢ��");
					}else if (tempY - startY <= 0) {
						// ���Ƶ�����
						state = DONE;
						changeHeaderViewByState();
						Log.i(TAG, "��DOne��������ˢ��״̬ת�䵽done״̬");
						LogUtils2.i("��DOne��������ˢ��״̬ת�䵽done״̬");
					}
				}
				
				/**
				 * ���������õ���ָ������ȥˢ�µ�ʱ��
				 */
				if (state != REFRESHING && isRecored && state != LOADING) {
					// ��������ȥˢ����
					if (state == RELEASE_To_REFRESH) {
						LogUtils2.i("*******************");
						canReturn = true;

						if (((tempY - startY) / RATIO < headContentHeight)
								&& (tempY - startY) > 0) {
							state = PULL_To_REFRESH;
							changeHeaderViewByState();
							Log.i(TAG, "���ɿ�ˢ��״̬ת�䵽����ˢ��״̬");
							LogUtils2.i("���ɿ�ˢ��״̬ת�䵽����ˢ��״̬");
						}
						// һ�����Ƶ�����
						else if (tempY - startY <= 0) {
							state = DONE;
							changeHeaderViewByState();
							Log.i(TAG, "���ɿ�ˢ��״̬ת�䵽done״̬");
							LogUtils2.i("���ɿ�ˢ��״̬ת�䵽done״̬");
						} else {
							// ���ý����ر�Ĳ�����ֻ�ø���paddingTop��ֵ������
						}
					}

					// ����headView��size
					if (state == PULL_To_REFRESH) {
						headView.setPadding(0, -1 * headContentHeight
								+ (tempY - startY) / RATIO, 0, 0);

					}

					// ����headView��paddingTop
					if (state == RELEASE_To_REFRESH) {
						headView.setPadding(0, (tempY - startY) / RATIO
								- headContentHeight, 0, 0);
					}
					
					
					if (canReturn) {
						canReturn = false;
						return true;
					}
				}
				break;
				
			case MotionEvent.ACTION_UP:
				if (state != REFRESHING && state != LOADING) {
					if (state == DONE) {
						// ʲô������
					}
					if (state == PULL_To_REFRESH) {
						state = DONE;
						changeHeaderViewByState();
						Log.i(TAG, "������ˢ��״̬����done״̬");
					}
					if (state == RELEASE_To_REFRESH) {
						state = REFRESHING;
						changeHeaderViewByState();
						LogUtils2.i("�������..onRefresh");
						onRefresh();
						Log.i(TAG, "���ɿ�ˢ��״̬����done״̬");
					}
				}
				isRecored = false;
				isBack = false;

				break;
				
			}
		}
		return super.onTouchEvent(event);
	}

	// ��״̬�ı�ʱ�򣬵��ø÷������Ը��½���
	private void changeHeaderViewByState() {
		switch (state) {
		case RELEASE_To_REFRESH:
			arrowImageView.setVisibility(View.VISIBLE);
			progressBar.setVisibility(View.GONE);
			tipsTextview.setVisibility(View.VISIBLE);
			lastUpdatedTextView.setVisibility(View.VISIBLE);

			arrowImageView.clearAnimation();
			arrowImageView.startAnimation(animation);

			tipsTextview.setText("�ɿ�ˢ��");

			LogUtils2.i("��ǰ״̬���ɿ�ˢ��*******");
			break;
		case PULL_To_REFRESH:
			progressBar.setVisibility(View.GONE);
			tipsTextview.setVisibility(View.VISIBLE);
			lastUpdatedTextView.setVisibility(View.VISIBLE);
			arrowImageView.clearAnimation();
			arrowImageView.setVisibility(View.VISIBLE);
			// ����RELEASE_To_REFRESH״̬ת������
			if (isBack) {
				isBack = false;
				arrowImageView.clearAnimation();
				arrowImageView.startAnimation(reverseAnimation);

				tipsTextview.setText("����ˢ��");
			} else {
				tipsTextview.setText("����ˢ��");
			}
			Log.i(TAG, "��ǰ״̬������ˢ��");
			LogUtils2.d("��ǰ״̬������ˢ��...");
			break;

		case REFRESHING:

			headView.setPadding(0, 0, 0, 0);

			progressBar.setVisibility(View.VISIBLE);
			arrowImageView.clearAnimation();
			arrowImageView.setVisibility(View.GONE);
			tipsTextview.setText("����ˢ��...");
			lastUpdatedTextView.setVisibility(View.VISIBLE);

			Log.i(TAG, "��ǰ״̬,����ˢ��...");
			LogUtils2.d("��ǰ״̬,����ˢ��...");
			break;
		case DONE:
			headView.setPadding(0, -1 * headContentHeight, 0, 0);

			progressBar.setVisibility(View.GONE);
			arrowImageView.clearAnimation();
			arrowImageView.setImageResource(R.drawable.goicon);
			tipsTextview.setText("����ˢ��");
			lastUpdatedTextView.setVisibility(View.VISIBLE);

			LogUtils2.i("��ǰ״̬��done......");
			break;
		}
	}

	/**
	 * MeasureSpec��װ��parent���ݸ�child��layoutҪ��ÿ��MeasureSpec��ʾ��width/height��Ҫ��
	 * MeasureSpec��size��mode��ɡ����õ�mode��3�֣�
	1. UNSPECIFIED��ʾparentû��ǿ�Ӹ�child�κ�constraint��
	2. EXACTLY��ʾparent�Ѿ�ȷ��child�ľ�ȷsize��
	3. AT_MOST��ʾchild�����趨Ϊspecified size֮�ڵ��κ�ֵ��
	 * @param child
	 */
	private void measureView(View child) {
		
		ViewGroup.LayoutParams p = child.getLayoutParams();
		if (p == null) {
			p = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT,
					ViewGroup.LayoutParams.WRAP_CONTENT);
		}
		
		/**
		 * ����Ӧ���ǲ�����ȡ��View�Ĵ�С�ѣ�Ȼ���ڸ�view�и������ʵĴ�С��ʾ
		 */
		LogUtils2.d("p.width=="+p.width+"   p.height=="+p.height);
		int childWidthSpec = ViewGroup.getChildMeasureSpec(0, 0 + 0, p.width);
		int lpHeight = p.height;
		int childHeightSpec;
		/**
		 * 
		 */
		if (lpHeight > 0) {
			childHeightSpec = MeasureSpec.makeMeasureSpec(lpHeight,
					MeasureSpec.EXACTLY);
		} else {
			childHeightSpec = MeasureSpec.makeMeasureSpec(0,
					MeasureSpec.UNSPECIFIED);
		}
		
		//
		LogUtils2.i("childWidthSpec=="+childWidthSpec+"   childHeightSpec="+childHeightSpec);
		child.measure(childWidthSpec, childHeightSpec);
	}

	public void setonRefreshListener(OnRefreshListener refreshListener) {
		this.refreshListener = refreshListener;
		isRefreshable = true;
	}

	public interface OnRefreshListener {
		public void onRefresh();
	}

	public void onRefreshComplete() {
		state = DONE;
		lastUpdatedTextView.setText("�������:" + new Date().toLocaleString());
		changeHeaderViewByState();
		invalidate();
		scrollTo(0, 0);
	}

	private void onRefresh() {
		if (refreshListener != null) {
			LogUtils2.i("��ʼ...........onRefresh");
			refreshListener.onRefresh();
		}
	}

	public void addChild(View child) {
		innerLayout.addView(child);
	}

	public void addChild(View child, int position) {
		innerLayout.addView(child, position);
	}
}
