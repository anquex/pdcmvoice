%clear all;
close all
clc

t=2.^-(1:32);
s=zeros(1,length(t));
s(1)=t(1);
for i=2:length(t)
    s(i)=s(i-1)+t(i);
end
s=[0 s];
  
c=10000;
nmax=26;

Lv=zeros(1,nmax+1);
Lv(1)=1;
Lv(2)=1;
for n=2:nmax
Lcum=0;
for count=1:c
    A=sort(rand(1,n));
    A;
    i=0;

    %A'
    reset=1;
    k=0;
    pe=0;
    while (k~=length(A))
        [k,i,L,pe]=cbtinit(A,k+1 ,pe,i);
        Lcum=Lcum+L;
        %k
        %pe
        %i
        %pause
        if (k==length(A-1))
            if(pe~=1)
                Lcum=Lcum+1;
            end;
          %  disp('Batch risolto');
            break;
            
        end
        while(pe>=s(reset))
            reset=reset+1;
        end
        i=reset-2;
        pe=s(reset-1);
        %pe
        %i
        %pause
    end
end
Lv(n+1)=Lcum./c;
end


%%%%%%%%%%%%%%


%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
p=0.4175;
Q=zeros(nmax+1); % Q(i+1,n+1) <- Q_i(n)

for n=0:nmax
    for i=0:n
        Q(i+1,n+1)=binopdf(i,n,p);
    end
end

L=zeros(nmax+1,1);
L(1)=1;
L(2)=1;

for n=2:nmax
    s=0;
    for i=1:n-1
        s=s+Q(i+1,n+1)*( L(i+1)+L(n-i+1) );
    end
    %for Basic BT
    %L(n+1)=(1+Q(0+1,n+1)+Q(n+1,n+1)+s)/(1-Q(0+1,n+1)-Q(n+1,n+1));
    %for MBT
    L(n+1)=(1+Q(n+1,n+1)+s)/(1-Q(0+1,n+1)-Q(n+1,n+1));
end



close all;
hold all;
h1=plot([0:nmax],Lv);
h2=plot([0:nmax],L);

legend('$L_n^{CMBT}$','$L_n^{M\!BT}\quad p$ opt','$L_n^{M\!BT}\quad p=0.4175$');
hl = legend;
set(hl, 'Interpreter', 'Latex');
set(hl, 'FontSize', 14);
set(hl, 'Location', 'NorthWest');

grid on;

hx=xlabel('Batch Size','Interpreter','Latex');
hy=ylabel('Expected Time in slots','Interpreter','Latex');
set(hx, 'FontSize', 15);
set(hy, 'FontSize', 15);

set(gca,'XTick',0:nmax);
set(gca,'YTick',0:1:50);

fh = figure(1); % returns the handle to the figure object
set(fh, 'color', 'white'); % sets the color to white

set(h1, 'LineStyle', '-', 'LineWidth', 1.0);
set(h1, 'Marker', 'o', 'MarkerFaceColor', [0.5 0.5 0.5], 'MarkerEdgeColor', [0 0 0], 'MarkerSize', 4.0);
set(h2, 'LineStyle', '-', 'LineWidth', 1.0);
set(h2, 'Marker', 's', 'MarkerFaceColor', [1 1 1], 'MarkerEdgeColor', [0 0 0], 'MarkerSize', 4.0);
%set(h3, 'LineStyle', '-', 'LineWidth', 1.0, 'Color',' Black');
%set(h3, 'Marker', 'd', 'MarkerFaceColor', [.8 .8 .8], 'MarkerEdgeColor', [0 0 0], 'MarkerSize', 5.0);

axis ([-0 6 0 17]);

h=findobj(gcf,'type','axes','tag','legend');
Pos=get(h,'position')
Pos(3)=1.05*Pos(3); % Double the length
set(h,'position',Pos) % Implement it

saveas(fh, 'CMBT-vs-MBT-small', 'epsc');

set(gca,'XTick',0:nmax);
set(gca,'YTick',0:5:50);

axis ([-0 19 0 55]);
saveas(fh, 'CMBT-vs-MBT-large', 'epsc');

